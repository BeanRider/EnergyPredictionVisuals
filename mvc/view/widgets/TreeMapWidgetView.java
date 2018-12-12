package mvc.view.widgets;

import mvc.model.datasource.TemporalNumberDS;
import mvc.model.datasource.Tuple;
import mvc.model.dimension.time.TimeRange;
import mvc.model.dimension.time.TimeUtil;
import mvc.model.widgets.IWidgetModel;
import mvc.view.viewmodel.CityVisualizationViewModel;
import visitors.widgetview.IWidgetViewVisitor;
import org.joda.time.DateTime;
import processing.core.PApplet;
import utility.voro.Point;
import utility.voro.Polygon;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by redbeans on 11/30/16.
 */
public class TreeMapWidgetView extends AbstractBoxWidgetView {

    private List<TemporalNumberDS<Float>> timeseriesData;
    private TimeRange renderTimeRange;

    public TreeMapWidgetView(List<TemporalNumberDS<Float>> timeseriesData, TimeRange renderTimeRange) {
        super(800, 800, "Treemap");
        this.timeseriesData = timeseriesData;
        this.renderTimeRange = renderTimeRange;
    }

    @Override
    public IWidgetModel.WidgetType getWidgetType() {
        return IWidgetModel.WidgetType.Treemap;
    }

    @Override
    public void accept(IWidgetViewVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Slice-and-dice: Starts at the top-level and works recursively down to the leaf level of the tree.
     * Suppose you have N values at any given level of the tree and a corresponding rectangle.
     a) Sort the values in descending order.
     b) Select the first k values (0<k<N) which sum to at least the split-ratio of the values total.
     c) Split the rectangle into two parts according to split-ratio along its longer side (to avoid very narrow shapes).
     d) Allocate the first k values to the split-off part, the remaining N-k values to the rest of the rectangle.
     e) Repeat as long as you have sublists with more than one value (N>1) at current level.
     f) For each node at current level, map its sub-tree onto the corresponding rectangle (until you reach leaf level).

     Example:
     {6,5,4,3,2,1}, sum = 21
     Split-ratio = 0.4, then we split the values into {6,5} and {4,3,2,1} since the ratio (6+5)/21 = 0.53 > 0.4,
     then continue with {6,5} in the first portion of the rectangle and with {4,3,2,1} in the other portion.
     * @param parentView
     * @param viewDataModel
     */
    @Override
    public void render(PApplet parentView, CityVisualizationViewModel viewDataModel) {

        pushPosition(parentView);
        parentView.pushStyle();

        List<Tuple<String, Float>> renderData = new ArrayList<>();
        int dataIndex = TimeUtil.computePeriodsBetween(
                renderTimeRange.toDateTimeFor(-1),
                renderTimeRange.toDateTimeFor(0),
                timeseriesData.get(0).getSampleInterval());
        for (int i = 0; i < timeseriesData.size(); ++i) {
            renderData.add(new Tuple(
                    timeseriesData.get(i).getValueColName(),
                    timeseriesData.get(i).requestValue_BoundedIndex(dataIndex).get().y));
        }

        // a) Sort the values in descending order.
        renderData = renderData.stream().filter(n -> n.y >= 0).collect(Collectors.toList());
        Collections.sort(renderData, new TreemapTupleRevCompare());

        float sum = sumTuple(renderData);
        float partialSum = 0;
        List<Tuple<String, Float>> a = new ArrayList<>();
        List<Tuple<String, Float>> b = new ArrayList<>();
        // b) Divide into two branches based on ratio
        for (int i = 0; i < renderData.size(); ++i) {
            partialSum += renderData.get(i).y;
            if (partialSum / sum > splitRatio) {
                // Divide into two parts: a, b
                a = renderData.subList(0, i + 1);
                b = renderData.subList(i + 1, renderData.size());
                break;
            }
        }

        // c) Start recursion to draw everything... base case for a branch is last element
        Polygon rect = new Polygon(new Point[] {
                new Point(0, 0, 0),
                new Point(super.w, 0, 0),
                new Point(super.w, super.h, 0),
                new Point(0, super.h, 0)
        });
        Polygon[] postSplit = splitWithRatio(partialSum / sum, true, rect);
        renderTreemap(a, b, postSplit[0], postSplit[1], false, (PApplet) parentView);

        parentView.popStyle();
        popPosition(parentView);
    }

    private float sumTuple(List<Tuple<String, Float>> toSum) {
        Optional<Float> possibleSum = toSum.stream().map(n -> n.y).reduce((x, y) -> x + y);
        if (possibleSum.isPresent()) {
            return possibleSum.get();
        } else {
            throw new RuntimeException();
        }
    }

    private Polygon[] splitWithRatio(float ratio, boolean isHorizontal, Polygon toSplit) {
        float minX = toSplit.points[0].x;
        float maxX = toSplit.points[2].x;
        float minY = toSplit.points[0].y;
        float maxY = toSplit.points[2].y;

        if (isHorizontal) {
            Point[] topPath = {
                    new Point(minX, minY, 0),
                    new Point(maxX, minY, 0),
                    new Point(maxX, minY + (maxY - minY) * ratio, 0),
                    new Point(minX, minY + (maxY - minY) * ratio, 0)
            };
            Polygon top = new Polygon(topPath);

            Point[] botPath = {
                    new Point(minX, minY + (maxY - minY) * ratio, 0),
                    new Point(maxX, minY + (maxY - minY) * ratio, 0),
                    new Point(maxX, maxY, 0),
                    new Point(minX, maxY, 0)
            };
            Polygon bot = new Polygon(botPath);
            return new Polygon[] {top, bot};
        } else {
            Point[] leftPath = {
                    new Point(minX, minY, 0),
                    new Point(minX + (maxX - minX) * ratio, minY, 0),
                    new Point(minX + (maxX - minX) * ratio, maxY, 0),
                    new Point(minX, maxY, 0)
            };
            Polygon left = new Polygon(leftPath);

            Point[] rightPath = {
                    new Point(minX + (maxX - minX) * ratio, minY, 0),
                    new Point(maxX, minY, 0),
                    new Point(maxX, maxY, 0),
                    new Point(minX + (maxX - minX) * ratio, maxY, 0)
            };
            Polygon right = new Polygon(rightPath);
            return new Polygon[] {left, right};

        }
    }

    float splitRatio = 0.4f;
    private void renderTreemap(List<Tuple<String, Float>> part1, List<Tuple<String, Float>> part2,
                               Polygon rect1, Polygon rect2,
                               boolean isHorizontal,
                               PApplet surface) {

        if (part1.size() == 1) {
            surface.strokeWeight(5);
            surface.stroke(255);

            int warmColors[] = {
                    surface.color(254,239,127),
                    surface.color(254, 234, 101),
                    surface.color(250, 209, 91),
                    surface.color(246, 185, 77),
                    surface.color(242, 161, 64),
                    surface.color(237, 137, 55),
                    surface.color(233, 115, 44),
                    surface.color(229, 92, 34),
                    surface.color(201, 78, 31),
                    surface.color(160, 68, 37),
                    surface.color(119, 62, 44),
                    surface.color(77, 52, 47)};
            int color;
            float toPlot = part1.get(0).y;
            color = warmColors[(int) ((toPlot / 0.5f) * (warmColors.length - 1))];
            color = 220 - (int) ((toPlot / 0.3f) * 220);
            surface.fill(color);
            rect1.render2D(surface);

            Point centroid = rect1.get_centroid_point();
            surface.fill(255);
            surface.textAlign(PApplet.CENTER, PApplet.CENTER);
            surface.text(part1.get(0).x.substring(0, 10) + "; " + part1.get(0).y,
                    centroid.x,
                    centroid.y);

        } else {
            float sum = sumTuple(part1);
            float partialSum = 0;
            List<Tuple<String, Float>> part1A = new ArrayList<>();
            List<Tuple<String, Float>> part1B = new ArrayList<>();
            // b) Divide into two branches based on ratio
            for (int i = 0; i < part1.size(); ++i) {
                partialSum += part1.get(i).y;
                if (partialSum / sum > splitRatio) {
                    // Divide into two parts: a, b
                    part1A = part1.subList(0, i + 1);
                    part1B = part1.subList(i + 1, part1.size());
                    break;
                }
            }
            Polygon[] postSplit = splitWithRatio(partialSum / sum, isHorizontal, rect1);
            renderTreemap(part1A, part1B, postSplit[0], postSplit[1], !isHorizontal, surface);
        }

        if (part2.size() == 1) {
            surface.strokeWeight(5);
            surface.stroke(255);
            int warmColors[] = {
                    surface.color(254,239,127),
                    surface.color(254, 234, 101),
                    surface.color(250, 209, 91),
                    surface.color(246, 185, 77),
                    surface.color(242, 161, 64),
                    surface.color(237, 137, 55),
                    surface.color(233, 115, 44),
                    surface.color(229, 92, 34),
                    surface.color(201, 78, 31),
                    surface.color(160, 68, 37),
                    surface.color(119, 62, 44),
                    surface.color(77, 52, 47)};
            int color;
            float toPlot = part2.get(0).y;
            color = warmColors[(int) ((toPlot / 0.5f) * (warmColors.length - 1))];
            color = 220 - (int) ((toPlot / 0.3f) * 220);
            surface.fill(color);
            rect2.render2D(surface);

            Point centroid = rect2.get_centroid_point();
            surface.fill(255);
            surface.textAlign(PApplet.CENTER, PApplet.CENTER);
            surface.text(part2.get(0).x.substring(0, 10) + "; " + part2.get(0).y,
                    centroid.x,
                    centroid.y);
        } else {
            float sum = sumTuple(part2);
            float partialSum = 0;
            List<Tuple<String, Float>> part2A = new ArrayList<>();
            List<Tuple<String, Float>> part2B = new ArrayList<>();
            // b) Divide into two branches based on ratio
            for (int i = 0; i < part2.size(); ++i) {
                partialSum += part2.get(i).y;
                if (partialSum / sum > splitRatio) {
                    // Divide into two parts: a, b
                    part2A = part2.subList(0, i + 1);
                    part2B = part2.subList(i + 1, part2.size());
                    break;
                }
            }
            Polygon[] postSplit = splitWithRatio(partialSum / sum, isHorizontal, rect2);
            renderTreemap(part2A, part2B, postSplit[0], postSplit[1], !isHorizontal, surface);
        }
    }

    class TreemapTupleRevCompare implements Comparator<Tuple<String, Float>> {
        @Override
        public int compare(Tuple<String, Float> o1, Tuple<String, Float> o2) {
            return Float.compare(o2.y, o1.y);
        }
    }

    @Override
    public void removeHoveredStates() {

    }

    @Override
    public void resetToDefault() {

    }

    @Override
    public void draw(PApplet s) {
        render(s, null);
    }

    public void updateTime(DateTime current) {
        renderTimeRange.setCurrentTime(renderTimeRange.toDateTimeFor(0).withDayOfYear(current.getDayOfYear()));
    }
}
