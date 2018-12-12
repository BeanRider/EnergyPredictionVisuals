package mvc.view.widgets;

import mvc.model.datasource.*;
import mvc.model.dimension.time.TimeRange;
import mvc.model.dimension.time.TimeUtil;
import mvc.model.widgets.IWidgetModel;
import mvc.view.viewmodel.CityVisualizationViewModel;
import utility.GeoUtil;
import visitors.widgetview.IWidgetViewVisitor;
import visitors.viewmodel.RCPViewModelDataVisitor;
import org.joda.time.DateTime;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.event.MouseEvent;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 *
 */
public class GeoSnapShotWidgetView extends AbstractBoxWidgetView implements IWidgetView {

  private TimeRange renderTimeRange;
  private Map<Integer, TemporalNumberDS<Float>> renderData;
  private Point2D.Float originLatLong;

  public GeoSnapShotWidgetView(TimeRange renderTimeRange, Point2D.Float originLatLong, Map<Integer, TemporalNumberDS<Float>> toRender, int w, int h) {
    super(w, h, "GeoSnapShot");
    this.renderTimeRange = renderTimeRange;
    this.originLatLong = originLatLong;
    this.renderData = toRender;
  }

  public GeoSnapShotWidgetView(TimeRange renderTimeRange, Point2D.Float originLatLong, Map<Integer, TemporalNumberDS<Float>> toRender) {
    super(1280, 800, "GeoSnapShot");
    this.renderTimeRange = renderTimeRange;
    this.originLatLong = originLatLong;
    this.renderData = toRender;
  }

  private PGraphics cachedGraphics;
  @Override
  public void draw(PApplet s) {
    pushPosition(s);
    drawBoundaries(s);
    s.image(cachedGraphics, 0, 0);
    popPosition(s);
  }

  /**
   * Assumes positional transformations (including margins) are done in the layout manager, in the view.
   * Draws with origin at 0, 0
   * Padding transformations are done within this method
   * @param renderer
   * @param viewDataModel
   */
  @Override
  public void render(PApplet renderer, CityVisualizationViewModel viewDataModel) {
    RCPViewModelDataVisitor visitor = new RCPViewModelDataVisitor("facilityOutlines");
    viewDataModel.accept(visitor);
    IData data = visitor.getResponse();

    IDataVisitor<Container<Map<Integer, List<PShape>>>> dataVisitor = new DataVisitor<>();
    data.accept(dataVisitor);
    Map<Integer, List<PShape>> facilityOutlines = dataVisitor.getResponse().unbox();

    visitor = new RCPViewModelDataVisitor("facilityCenters");
    viewDataModel.accept(visitor);
    data = visitor.getResponse();

    IDataVisitor<Container<List<Point2D.Float>>> listVisitor = new DataVisitor<>();
    data.accept(listVisitor);
    List<Point2D.Float> facilityCenters = listVisitor.getResponse().unbox();

    int warmColors[] = {
            renderer.color(254,239,127),
            renderer.color(254, 234, 101),
            renderer.color(250, 209, 91),
            renderer.color(246, 185, 77),
            renderer.color(242, 161, 64),
            renderer.color(237, 137, 55),
            renderer.color(233, 115, 44),
            renderer.color(229, 92, 34),
            renderer.color(201, 78, 31),
            renderer.color(160, 68, 37),
            renderer.color(119, 62, 44),
            renderer.color(77, 52, 47)};
    int coolColors[] = {
            renderer.color(220, 236, 200),
            renderer.color(170, 220, 204),
            renderer.color(118, 199, 209),
            renderer.color(71, 179, 213),
            renderer.color(59, 147, 194),
            renderer.color(48, 115, 175),
            renderer.color(42, 84, 156),
            renderer.color(29, 51, 136),
            renderer.color(23, 29, 109),
            renderer.color(15, 20, 76)};

    cachedGraphics = renderer.createGraphics(w, h);
    cachedGraphics.beginDraw();
    cachedGraphics.pushMatrix();
    cachedGraphics.pushStyle();
    cachedGraphics.translate(-200, -200);

    Set<Integer> indexesWithData = renderData.keySet();

    // Shapes with no data
    for (Integer s : facilityOutlines.keySet()) {
      if (!indexesWithData.contains(s)) {
        for (PShape shape : facilityOutlines.get(s)) {
          shape.setFill(false);
          shape.setStroke(255);
          cachedGraphics.shape(shape);
        }
      }
    }


    for (Integer i : indexesWithData) {
      int fIdx = i;
      float centroidX = facilityCenters.get(fIdx).x;
      float centroidY = facilityCenters.get(fIdx).y;
      TemporalNumberDS<Float> dataForI  = renderData.get(i);
      List<PShape> thisFacilityShapes = facilityOutlines.get(fIdx);

      cachedGraphics.fill(79);
      cachedGraphics.noStroke();

      for (PShape shape : thisFacilityShapes) {
        int color;
        int dataIndex = TimeUtil.computePeriodsBetween(
                renderTimeRange.toDateTimeFor(TimeRange.START),
                renderTimeRange.toDateTimeFor(TimeRange.CURRENT),
                dataForI.getSampleInterval());

        Optional<Tuple<Long, Float>> currentTuple = dataForI.requestValue_BoundedIndex(dataIndex);
        if (!currentTuple.isPresent()) {
          shape.setFill(255);
          cachedGraphics.shape(shape);
        } else {
          float toPlot = currentTuple.get().y;
          float colorScale = 0.8f;
          if (toPlot > 0) {
            color = warmColors[(int) ((toPlot / colorScale) * (warmColors.length - 1))]; // FIXME
          } else {
            color = coolColors[(int) (Math.abs(toPlot / colorScale) * (coolColors.length - 1))];
          }
          shape.setFill(color);
          shape.setStrokeWeight(0);
          cachedGraphics.shape(shape);

          // Label
//          cachedGraphics.strokeWeight(1);
//          cachedGraphics.stroke(0);
//          cachedGraphics.line(centroidX, centroidY, centroidX - 40, centroidY - 60);
//
//          float delta = dataForI.getDataTable().getFloatColumn(3)[dataIndex] - dataForI.getDataTable().getFloatColumn(2)[dataIndex];
//          BigDecimal bd = new BigDecimal(Float.toString(Math.abs(currentTuple.get().y * 100)));
//          bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
//          BigDecimal bd2 = new BigDecimal(Float.toString(delta));
//          bd2 = bd2.setScale(3, BigDecimal.ROUND_HALF_UP);
//          String labelText;
////        app.textFont(RCPVisualizer_DynamicView.f);
//          if (currentTuple.get().y > 0) {
//            labelText = bd2.floatValue() + " (+ " + bd.floatValue()+ "%)";
//          } else {
//            labelText = bd2.floatValue() + " (- "  + bd.floatValue()+ "%)";
//          }
//
//          int labelTextWidth = (int) cachedGraphics.textWidth(labelText);
//
//          cachedGraphics.pushMatrix();
//          cachedGraphics.translate(centroidX - 40 - (labelTextWidth + 10), centroidY - 60 - 19, +1);
//          cachedGraphics.rectMode(PConstants.CORNERS);
//          cachedGraphics.fill(0, 210);
//          cachedGraphics.rect(0, 0, labelTextWidth + 10, 19, 3);
//
//          // FIXME change data source to have multiple column mappings
//          if (currentTuple.get().y > 0) {
//            cachedGraphics.fill(34, 237, 34);
//          } else if (Math.abs(currentTuple.get().y) < 0.000001f) {
//            cachedGraphics.fill(255);
//          } else {
//            cachedGraphics.fill(237, 28, 36);
//          }
//
//          cachedGraphics.text(labelText, 5, 12);
////        app.text(bd.floatValue()+ "%", 5, 30);
//          cachedGraphics.popMatrix();
        }

        // Draw centroid dot
        cachedGraphics.fill(75);
        cachedGraphics.ellipse(centroidX, centroidY, 5, 5);


      }
    }
    // Draw hotspots
    cachedGraphics.fill(255, 0,0, 160);
    Point2D.Float entranceRyder = GeoUtil.convertGeoToScreen(
            new Point2D.Float(42.33679f, -71.09057f),
            originLatLong,
            renderer.displayWidth,
            renderer.displayHeight);
    cachedGraphics.ellipse(entranceRyder.x, entranceRyder.y, 15, 15);
    Point2D.Float behrakisEntrance = GeoUtil.convertGeoToScreen(
            new Point2D.Float(42.33711f, -71.09149f),
            originLatLong,
            renderer.displayWidth,
            renderer.displayHeight);
    cachedGraphics.ellipse(behrakisEntrance.x, behrakisEntrance.y, 15, 15);
    Point2D.Float shillmanEntrance = GeoUtil.convertGeoToScreen(
            new Point2D.Float(42.33743f, -71.09011f),
            originLatLong,
            renderer.displayWidth,
            renderer.displayHeight);
    cachedGraphics.ellipse(shillmanEntrance.x, shillmanEntrance.y, 15, 15);
    cachedGraphics.popStyle();
    cachedGraphics.popMatrix();
    cachedGraphics.endDraw();
  }

  @Override
  public void checkState(MouseEvent e) {
    // DO NOTHING
  }

  @Override
  public void removeHoveredStates() {
//    showTooltip = false;
  }

  @Override
  public void resetToDefault() {
    // DO NOTHING
  }

  @Override
  public IWidgetModel.WidgetType getWidgetType() {
    return IWidgetModel.WidgetType.GeoSnapShot;
  }

  @Override
  public void accept(IWidgetViewVisitor visitor) {
    visitor.visit(this);
  }

  public void updateTime(DateTime current) {
    renderTimeRange.setCurrentTime(renderTimeRange.toDateTimeFor(0).withDayOfYear(current.getDayOfYear()));
  }
}
