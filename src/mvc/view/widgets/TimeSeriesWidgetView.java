package mvc.view.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.datasource.Tuple;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.IWidgetModel;
import mvc.view.viewmodel.CityVisualizationViewModel;
import visitors.widgetview.IWidgetViewVisitor;
import org.joda.time.DateTime;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Optional;

/**
 *
 */
public class TimeSeriesWidgetView extends AbstractBoxWidgetView implements IWidgetView {

  private TimeRange renderTimeRange;
  private DataSource<Float> renderData;
  private PGraphics surface;

  public TimeSeriesWidgetView(TimeRange renderTimeRange, DataSource toRender, int w, int h) {
    super(w, h, toRender.getValueColName());
    this.renderTimeRange = renderTimeRange;
    this.renderData = toRender;
  }

  public TimeSeriesWidgetView(TimeRange renderTimeRange, DataSource toRender) {
    super(50, 50, toRender.getValueColName());
    this.renderTimeRange = renderTimeRange;
    this.renderData = toRender;
  }

  @Override
  public void draw(PApplet s) {
    PApplet app = s;
    app.image(surface, cornerXY.x, cornerXY.y);

    // Hover tooltip
    if (showTooltip) {

      app.pushMatrix();
      app.pushStyle();

      app.translate(
              cornerXY.x + padding.getL() + 50 + halfBarWidth,
              cornerXY.y + getDimension().height / 2);

      // 1. Calculate which bar it is at
      float indexCalc = (app.mouseX - cornerXY.x - padding.getL() - 50 - halfBarWidth) / (float) (barWidth*365 + barGap*364);
      int index = Math.round(indexCalc * 365);

      Optional<Tuple<Long, Float>> tuple = renderData.requestValue_BoundedIndex(index);

      float value;
      long unixValue;

      if (tuple.isPresent()) {
        value = tuple.get().y;
        unixValue = tuple.get().x;
        int xPos = (barWidth + barGap) * index;
        int yPos;
        if (value > 0) {
          yPos = Math.round(-value * renderScale - barWidth);
        } else {
          yPos = Math.round(-value * renderScale + barGap);
        }
        app.fill(40, 175);
        app.stroke(40, 200);
        app.rectMode(PConstants.CORNER);
        app.rect(xPos, yPos, 64 + 50, 35);
        app.fill(255);
        BigDecimal bd = new BigDecimal(Float.toString(value / 4f));
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        app.text(bd.floatValue() + " kW / ft^2", xPos + 4, yPos + 30);
        DateTime t = new DateTime(unixValue * 1000L);
        app.text(t.monthOfYear().getAsShortText().toUpperCase()+ " " + t.getDayOfMonth(), xPos + 4, yPos + 14);

      }
      app.popStyle();
      app.popMatrix();
    }
  }

  // Settings for monthly
  float renderScale = 2;
  int barWidth = 45;
  int barGap = 20;

  // Setting for weekly
//  float renderScale = 2;
//  int barWidth = 12;
//  int barGap = 10;

  int halfBarWidth = Math.round(barWidth / 2f);

  /**
   * Assumes positional transformations (including margins) are done in the layout manager, in the view.
   * Draws with origin at 0, 0
   * Padding transformations are done within this method
   * @param s
   * @param viewDataModel
   */
  @Override
  public void render(PApplet s, CityVisualizationViewModel viewDataModel) {
    PApplet sur = s;

    surface = sur.createGraphics(w + 1, h + 1); // plus one because of stroke
    surface.beginDraw();
    surface.pushStyle();

    surface.pushMatrix();
    // new origin is top left corner after padding.
    Dimension dim = getDimension();

    surface.fill(255);
    surface.strokeWeight(1f);
    surface.stroke(200);
    surface.rectMode(PConstants.CORNERS);
    surface.rect(0, 0, dim.width, dim.height);

    surface.fill(150);
    surface.text(widgetID, 10, 20);

    int paddedWidth = dim.width - super.padding.getL() - super.padding.getR();
    int paddedHeight = dim.height - super.padding.getT() - super.padding.getB();

    // Start padded content
    surface.pushMatrix();
    // new origin is top left corner after padding.

    surface.translate(0 + super.padding.getL(), 0 + super.padding.getR());

    // Graph x line
    Float max = renderData.getMaxVal().get();
    Float min = renderData.getMinVal().get();
    int centerX = paddedWidth / 2;
    int centerY = paddedHeight / 2;

    surface.textAlign(PConstants.LEFT, PConstants.CENTER);
    surface.text(max / 4f, 0, centerY - max * renderScale); // div 4 to adjust for error
    surface.text(min / 4f, 0, centerY - min * renderScale); // div 4 to adjust for error

    surface.pushMatrix();
    surface.translate(50, 0);

    surface.line(0, centerY, paddedWidth, centerY);

    surface.stroke(240);
    surface.line(
            0, centerY - max * renderScale,
            paddedWidth, centerY - max * renderScale);
    surface.line(
            0, centerY - min * renderScale,
            paddedWidth, centerY - min * renderScale);

    surface.rectMode(PConstants.CORNERS);
    surface.noStroke();
    surface.fill(20);

    int i = 0;
    for (Optional<Tuple<Long, Float>> value : renderData.getIndexedArray()) {
      if (!value.isPresent()) {
        i++;
        continue;
      }

      System.out.println("Value at " + i + " = " + value.get() +  "!");
//      System.out.println(value.get());
      int x = i * (barWidth + barGap);
//      System.out.println("i = " + i + "; x = " + x);
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
      int coolColors[] = {
              surface.color(220, 236, 200),
              surface.color(170, 220, 204),
              surface.color(118, 199, 209),
              surface.color(71, 179, 213),

              surface.color(59, 147, 194),
              surface.color(48, 115, 175),
              surface.color(42, 84, 156),
              surface.color(29, 51, 136),
              surface.color(23, 29, 109),
              surface.color(15, 20, 76)};
      int color;
      float toPlot = value.get().y;
      if (toPlot > 0) {
        color = warmColors[(int) ((toPlot / max) * (warmColors.length - 1))];
      } else {
        color = coolColors[(int) ((toPlot / min) * (coolColors.length - 1))];
      }
      surface.fill(color);
      surface.rect(x, centerY - toPlot * renderScale, x + barWidth, centerY);
      i++;
    }

    surface.popMatrix();

    surface.popMatrix();

    surface.popMatrix();

    surface.popStyle();
    surface.endDraw();
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
    return IWidgetModel.WidgetType.TimeSeries;
  }

  @Override
  public void accept(IWidgetViewVisitor visitor) {
    visitor.visit(this);
  }
}
