package mvc.view.widgets;

import mvc.model.datasource.*;
import mvc.model.widgets.IWidgetModel;
import mvc.view.viewmodel.CityVisualizationViewModel;
import org.joda.time.DateTime;
import processing.core.PApplet;
import processing.core.PGraphics;
import utility.chart.AxisType;
import utility.chart.ChartComponentType;
import utility.chart.AxisLabelSettings;
import utility.TableVectorRange;
import visitors.widgetview.IWidgetViewVisitor;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static processing.core.PConstants.*;

/**
 * Created by redbeans on 12/28/16.
 */
public class ViolinPlotWidgetView<T extends Number & Comparable<? super T>> extends AbstractBoxWidgetView {

    // Facility ID to data set
    private Map<Integer, TemporalNumberDS<T>> dataSource;

    // Facility ID to data set
    private Map<Integer, TemporalNumberDS<T>> base;

    List<String> names;
    List<Float> areas;

    private Optional<T> dataMin;
    private Optional<T> dataMax;
    private Optional<Long> dataMinUnix;
    private Optional<Long> dataMaxUnix;

    // Cached buffer
    private PGraphics buffer;

    // Tweakable Settings
    private Map<ChartComponentType, TableVectorRange> compToTableVectorMap = new HashMap<>();
    private Map<AxisType, AxisLabelSettings> axisToLabelSettings = new HashMap<>(); // visibility and frequency

    private int[] facilitiesToRender = {21, 27, 12, 40, 15, 14, 10}; // TODO implement range/selected
    // Lake, Richards, Ell, wve, hayden, forsyth, dodge

    /**
     * Based on given x and y label counts, set up a suitable label interval that shows these amounts of labels on the
     * chart. Calculations are based on max/min of the data set.
     * @param axis the axis to setup
     * @param numLabels number of desired labels on the y-axis
     * @param dataRange numerical data range available to use
     */
    private void autoAxisSetup(AxisType axis, int numLabels, Tuple<Number, Number> dataRange) {
        axisToLabelSettings.put(
                axis,
                new AxisLabelSettings.Builder()
                .render(true)
                .interval((dataRange.y.floatValue() - dataRange.x.floatValue()) / numLabels)
                .visualRange(new Tuple<>(dataRange.x.doubleValue(), dataRange.y.doubleValue()))
                .buildRegular());
    }

    private void detectDataMaxMin() {
        dataMin = Optional.empty();
        dataMax = Optional.empty();
        dataMinUnix = Optional.empty();
        dataMaxUnix = Optional.empty();

        for (Integer i : dataSource.keySet()) {
            TemporalNumberDS<T> d = dataSource.get(i);
            Optional<T> max = d.getMaxVal();
            Optional<T> min = d.getMinVal();
            Optional<Long> maxUnix = d.getLocalMaxUnix();
            Optional<Long> minUnix = d.getLocalMinUnix();

            if (max.isPresent() ^ min.isPresent() || maxUnix.isPresent() ^ minUnix.isPresent()) {
                throw new RuntimeException("Only one of max and min value/unix is present!");
            }

            if (max.isPresent() && min.isPresent()) {
                T thisMax = max.get();
                T thisMin = min.get();
                if (!dataMax.isPresent() || thisMax.compareTo(dataMax.get()) > 0) {
                    dataMax = Optional.of(thisMax);
                }
                if (!dataMin.isPresent() || thisMin.compareTo(dataMin.get()) < 0) {
                    dataMin = Optional.of(thisMin);
                }
            }

            if (maxUnix.isPresent() && minUnix.isPresent()) {
                long thisMaxUnix = maxUnix.get();
                long thisMinUnix = minUnix.get();
                if (!dataMaxUnix.isPresent() || thisMaxUnix > dataMaxUnix.get()) {
                    dataMaxUnix = Optional.of(thisMaxUnix);
                }
                if (!dataMinUnix.isPresent() || thisMinUnix < dataMinUnix.get()) {
                    dataMinUnix = Optional.of(thisMinUnix);
                }
            }
        }
    }

    public ViolinPlotWidgetView(Map<Integer, TemporalNumberDS<T>> dataSource, Map<Integer, TemporalNumberDS<T>> base) {
        super(1600, 600, "id");
        this.dataSource = dataSource;
        this.base = base;
    }

    @Override
    public IWidgetModel.WidgetType getWidgetType() {
        return IWidgetModel.WidgetType.Violin;
    }

    @Override
    public void accept(IWidgetViewVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void render(PApplet parentView, CityVisualizationViewModel viewDataModel) {
        // Calculate data max/min
        detectDataMaxMin();

        // Verify data
        if (!dataMin.isPresent() || !dataMax.isPresent() || !dataMinUnix.isPresent() || !dataMaxUnix.isPresent()) {
            buffer = parentView.createGraphics(super.w, super.h);
            buffer.beginDraw();
            buffer.fill(240);
            buffer.rect(0, 0, super.w, super.h);
            buffer.textAlign(CENTER, CENTER);
            buffer.text("DATA LACKING MIN/MIN FOR VALUES/UNIX", super.w / 2, super.h / 2);
            buffer.endDraw();
            return;
        }

        // Data bounds init:
        // for x axis (enumeration)
        facilitiesToRender = new int[] {21, 27, 12, 40, 15, 14, 10}; // TODO implement range/enumeration type
        // for violin axis (range)
        Tuple<T, T> violinAxisDataRange = new Tuple<>(dataMin.get(), dataMax.get());
        // for y axis (range)
        Tuple<Number, Number> yAxisDataRange = new Tuple<>(dataMinUnix.get(), dataMaxUnix.get());

        // Set up default (detected depending on data) visual ranges:
//        autoAxisSetup(AxisType.Y, 12, yAxisDataRange);
        axisToLabelSettings.put(AxisType.Y, new AxisLabelSettings.Builder()
                        .render(true)
                        .visualRange(new Tuple<>(yAxisDataRange.x.doubleValue(), yAxisDataRange.y.doubleValue()))
                        .mapFunction((x) -> (double) (new DateTime((long) x * 1000L).getMonthOfYear()))
                        .buildFunction());
        axisToLabelSettings.put(AxisType.X, new AxisLabelSettings.Builder().buildRegular()); // 0 - 10 with ticks of 1
        axisToLabelSettings.put(AxisType.SUB_X, new AxisLabelSettings.Builder().render(false).buildRegular());

        buffer = parentView.createGraphics(super.w, super.h);
        buffer.beginDraw();

        int green = buffer.color(19, 168, 16);
        int red = buffer.color(168, 42, 16);

        // Background
        buffer.fill(240);
        buffer.rect(0, 0, super.w, super.h);

        // Chart Area (padded)
        Dimension chartArea = super.calcPaddedDimensions();

        super.pushPadding(buffer);

        // Chart Background
        buffer.stroke(100);
        buffer.rect(0, 0, chartArea.width, chartArea.height);

        // Chart Titles
        Dimension leftTitleArea = new Dimension(50, 100);
        Point2D.Float leftTitlePlacement = calcLeftCenterPlacement(chartArea, leftTitleArea);
        boolean debug = true;
        if (debug) {
            buffer.rect(leftTitlePlacement.x, leftTitlePlacement.y,
                    leftTitleArea.width, leftTitleArea.height);
        }
        buffer.pushStyle();
        buffer.fill(10);
        buffer.textAlign(CENTER, CENTER);
        buffer.pushMatrix();
        buffer.translate(leftTitlePlacement.x + leftTitleArea.width / 2, leftTitlePlacement.y + leftTitleArea.height / 2);
        buffer.rotate(-HALF_PI);
        buffer.text("Month of Year", 0, 0);
        buffer.popMatrix();
        buffer.popStyle();


        Dimension rightTitleArea = new Dimension(50, 100);
        Point2D.Float rightTitlePlacement = calcRightCenterPlacement(chartArea, rightTitleArea);
        if (debug) {
            buffer.rect(rightTitlePlacement.x, rightTitlePlacement.y,
                    rightTitleArea.width, rightTitleArea.height);
        }
        Dimension bottomTitleArea = new Dimension(100, 50);
        Point2D.Float bottomTitlePlacement = calcBottomCenterPlacement(chartArea, bottomTitleArea);
        if (debug) {
            buffer.rect(bottomTitlePlacement.x, bottomTitlePlacement.y,
                    bottomTitleArea.width, bottomTitleArea.height);
        }
        buffer.pushStyle();
        buffer.fill(10);
        buffer.textAlign(CENTER, CENTER);
        buffer.pushMatrix();
        buffer.translate(bottomTitlePlacement.x + bottomTitleArea.width / 2, bottomTitlePlacement.y + bottomTitleArea.height / 2);
        buffer.text("Facility (% delta kWh/sqft, - | +)", 0, 0);
        buffer.popMatrix();
        buffer.popStyle();

        Dimension topTitleArea = new Dimension(100, 50);
        Point2D.Float topTitlePlacement = calcTopCenterPlacement(chartArea, topTitleArea);
        if (debug) {
            buffer.rect(topTitlePlacement.x, topTitlePlacement.y,
                    topTitleArea.width, topTitleArea.height);
        }
        buffer.fill(10);
        buffer.textAlign(CENTER, CENTER);
        buffer.text("Temporal Distribution of % Delta kWh/sqft by Facility (2013 vs. 2030)",
                chartArea.width / 2, topTitleArea.height / 2);
        buffer.text("Temporal Distribution of Absolute Delta kWh/sqft by Facility (2013 vs. 2030)",
                chartArea.width / 2, topTitleArea.height / 2);
        buffer.text("Temporal Distribution of kWh/sqft by Facility (2013 vs. 2030)",
                chartArea.width / 2, topTitleArea.height / 2);

        buffer.text("Temporal Distribution of kWh by Facility (2013 vs. 2030)",
                chartArea.width / 2, topTitleArea.height / 2);
        buffer.text("Temporal Distribution of Absolute Delta kWh by Facility (2013 vs. 2030)",
                chartArea.width / 2, topTitleArea.height / 2);

        // Chart AxisType Labels
        buffer.pushMatrix();
        buffer.translate(leftTitleArea.width, topTitleArea.height);
        Dimension chartLabelArea = new Dimension(
                chartArea.width - leftTitleArea.width - rightTitleArea.width,
                chartArea.height - topTitleArea.height - bottomTitleArea.height);
        Dimension yAxisArea = new Dimension(50, 300);
        Point2D.Float yAxisPlacement = calcLeftCenterPlacement(chartLabelArea, yAxisArea);
        buffer.pushStyle();
        buffer.stroke(100);
        buffer.fill(240);
        if (debug) {
            buffer.rect(yAxisPlacement.x, yAxisPlacement.y, yAxisArea.width, yAxisArea.height);
        }
        buffer.popStyle();

        float yAxisLabelSpacing;
        switch (axisToLabelSettings.get(AxisType.Y).getTickMode()) {
            case ALL_DATA:
                // TODO implement
                yAxisLabelSpacing = yAxisArea.height / 1f;
                throw new UnsupportedOperationException("Did not implement ALL_DATA axis label settings yet.");
            case REGULAR:
                yAxisLabelSpacing = yAxisArea.height / (float) (axisToLabelSettings.get(AxisType.Y).calculateNumTicks() - 1);
                break;
            case FUNCTIONAL:
                yAxisLabelSpacing = yAxisArea.height / (float) (axisToLabelSettings.get(AxisType.Y).calculateNumTicks() - 1);
                break;
            default:
                throw new UnsupportedOperationException("Did not implement this axis label settings yet.");
        }

        buffer.textAlign(LEFT, CENTER);
        for (int i = 0; i < axisToLabelSettings.get(AxisType.Y).calculateNumTicks(); ++i) {
            // draw from max label to min
            long label = Math.round(axisToLabelSettings.get(AxisType.Y).getUpperBound() - axisToLabelSettings.get(AxisType.Y).getInterval() * i);
            buffer.text(new DateTime(2014, (int) label, 1, 0, 0).monthOfYear().getAsText(), yAxisPlacement.x, yAxisPlacement.y + yAxisLabelSpacing * i);
        }

        // Graph Area
        buffer.pushMatrix();
        buffer.translate(yAxisPlacement.x + yAxisArea.width, yAxisPlacement.y);
        buffer.ellipseMode(CENTER);

        buffer.textAlign(CENTER, CENTER);

        IDataVisitor<mvc.model.datasource.Container<List<String>>> dataVisitor = new DataVisitor<>();
        viewDataModel.get("facilityNames").accept(dataVisitor);
        dataVisitor.getResponse().unbox();
        names = dataVisitor.getResponse().unbox();

        IDataVisitor<mvc.model.datasource.Container<List<Float>>> areaVisitor = new DataVisitor<>();
        viewDataModel.get("facilityAreas").accept(areaVisitor);
        areaVisitor.getResponse().unbox();
        areas = areaVisitor.getResponse().unbox();

        // TODO change this array to axis settings
        // No sides
//        for (int i = 0; i < facilitiesToRender.length; ++i) {
//            int toRender = facilitiesToRender[i];
//            buffer.pushMatrix();
//            buffer.translate(100 + i * 200, 0);
//            renderViolinPlotAt(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender);
//            buffer.text(names.get(toRender), 0, yAxisArea.height + 20);
//            buffer.popMatrix();
//        }

//        // Sides
//        for (int i = 0; i < facilitiesToRender.length; ++i) {
//            int toRender = facilitiesToRender[i];
//            buffer.pushMatrix();
//            buffer.translate(100 + i * 200, 0);
//            List<Optional<Tuple<Long, T>>> p = dataSource.get(toRender).getIndexedArray();
//            List<Optional<Tuple<Long, T>>> b = base.get(toRender).getIndexedArray();
//            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, true, false, green, b);
//            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, false, false, red, p);
//            buffer.text(names.get(toRender), 0, yAxisArea.height + 20);
//            buffer.popMatrix();
//        }

        // Mirror
//        for (int i = 0; i < facilitiesToRender.length; ++i) {
//            int toRender = facilitiesToRender[i];
//            buffer.pushMatrix();
//            buffer.translate(100 + i * 200, 0);
//            List<Optional<Tuple<Long, T>>> p = dataSource.get(toRender).getIndexedArray();

//            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, true, false, green, p);
//            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, false, false, green, p);
//            buffer.text(names.get(toRender), 0, yAxisArea.height + 20);
//            buffer.popMatrix();
//        }

        // Superimposed mirror kWh (2030 is red, 2013 is green)
        for (int i = 0; i < facilitiesToRender.length; ++i) {
            int toRender = facilitiesToRender[i];
            buffer.pushMatrix();
            buffer.translate(100 + i * 200, 0);
            List<Optional<Tuple<Long, T>>> p = dataSource.get(toRender).getIndexedArray();
            List<Optional<Tuple<Long, T>>> b = base.get(toRender).getIndexedArray();
            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, true, true, green, b);
            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, false, true, green, b);
            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, true, true, red, p);
            renderViolinPlotAtSym(yAxisArea.height, axisToLabelSettings.get(AxisType.Y), yAxisLabelSpacing, toRender, false, true, red, p);
            buffer.text(names.get(toRender), 0, yAxisArea.height + 20);
            buffer.popMatrix();
        }

        buffer.popMatrix();

        buffer.popMatrix();

        super.popPadding(buffer);

        buffer.endDraw();
    }

    private void renderViolinPlotAtSym(int height,
                                       AxisLabelSettings yAxisSettings,
                                       float yAxisLabelSpacing,
                                       int id, boolean isLeft, boolean kwh, int color,
                                       List<Optional<Tuple<Long, T>>> ls) {
        buffer.line(0, 0, 0, height);

        buffer.pushStyle();
        buffer.beginShape();
        buffer.fill(color, 20);
        buffer.stroke(color, 240);
        buffer.strokeWeight(2);
        Point2D.Float lastPoint = null;
        for (Optional<Tuple<Long, T>> t : ls) {
            if (!t.isPresent()) {
                continue;
            }
            Tuple<Long, T> tuple = t.get();
            long unixValue = tuple.x;
            double valueAtUnix = tuple.y.doubleValue();
            if (kwh) {
                valueAtUnix = valueAtUnix* areas.get(id);
            }
            float yPos = (float) (((yAxisSettings.getBounds().y - yAxisSettings.getMap().apply(unixValue)) / yAxisSettings.getInterval()) * yAxisLabelSpacing);
            float xPos;
            if (isLeft) {
                xPos = (float) (0 - (valueAtUnix * 0.00001f)); // 500 was scale for percentage; 0.06f = kWh/sqft; 0.00001f = kWh
            } else {
                xPos = (float) (0 + (valueAtUnix * 0.00001f)); // 500 was scale for percentage; 0.06f = kWh/sqft; 0.00001f = kWh
            }

            if (lastPoint == null) {
                buffer.curveVertex(0, yPos);
                buffer.curveVertex(0, yPos);
                buffer.curveVertex(xPos, yPos);
            } else {
                buffer.curveVertex(xPos, yPos);
            }
            buffer.pushStyle();
            buffer.fill(240);
            buffer.ellipse(xPos, yPos, 5, 5);
            buffer.popStyle();
            lastPoint = new Point2D.Float(xPos, yPos);
        }
//        buffer.curveVertex(lastPoint.x, lastPoint.y); // is also the last control point
        buffer.curveVertex(0, lastPoint.y);
        buffer.curveVertex(0, lastPoint.y);
        buffer.endShape();
        buffer.popStyle();
    }

    private void renderViolinPlotAt(int h, AxisLabelSettings yAxisSettings, float yPixelSpacing, int id) {
        buffer.line(0, 0, 0, h);

        buffer.pushStyle();
        buffer.beginShape();
        buffer.fill(19, 168, 16, 20);
        buffer.stroke(19, 168, 16, 240);
        buffer.strokeWeight(2);
        Point2D.Float lastPoint = null;
        for (Optional<Tuple<Long, T>> t : dataSource.get(id).getIndexedArray()) {
            if (!t.isPresent()) {
                continue;
            }
            Tuple<Long, T> tuple = t.get();
            long unixValue = tuple.x;
            T valueAtUnix = tuple.y;
            DateTime thisDateTime = new DateTime(unixValue * 1000L);
            long thisUnix = thisDateTime.getMillis() / 1000L;
            float yPos = (float) (((yAxisSettings.getBounds().y - yAxisSettings.getMap().apply(thisUnix)) / yAxisSettings.getInterval()) * yPixelSpacing);
            float xPos = (float) (0 + (valueAtUnix.doubleValue() * 500f)); // 500 was scale for percentage; 0.1f = kWh/sqft

            if (lastPoint == null) {
                buffer.curveVertex(0, yPos);
                buffer.curveVertex(0, yPos);
                buffer.curveVertex(xPos, yPos);
            } else {
                buffer.curveVertex(xPos, yPos);
            }
            buffer.pushStyle();
            buffer.fill(240);
            buffer.ellipse(xPos, yPos, 5, 5);
            buffer.popStyle();
            lastPoint = new Point2D.Float(xPos, yPos);
        }
//        buffer.curveVertex(lastPoint.x, lastPoint.y); // is also the last control point
        buffer.curveVertex(0, lastPoint.y);
        buffer.curveVertex(0, lastPoint.y);
        buffer.endShape();
        buffer.popStyle();
    }

    @Override
    public void removeHoveredStates() {

    }

    @Override
    public void resetToDefault() {

    }

    @Override
    public void draw(PApplet s) {
        this.pushPosition(s);
        s.image(buffer, 0, 0);
        this.popPosition(s);
    }
}
