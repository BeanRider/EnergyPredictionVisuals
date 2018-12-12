package mvc.view.widgets;

import mvc.view.Display;
import mvc.view.Margins;
import mvc.view.Padding;
import mvc.view.RectInteractable;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by jeffrey02px2014 on 10/16/16.
 */
public abstract class AbstractBoxWidgetView extends RectInteractable implements IWidgetView {

  protected Margins margins = new Margins(0);
  protected Padding padding = new Padding(10);

  protected Display display = Display.BLOCK;
  protected String widgetID = "";

  protected boolean showTooltip = false;

  protected AbstractBoxWidgetView(int width, int height, String widgetID) {
    super(width, height);
    this.widgetID = widgetID;
  }

  protected void pushPosition(PApplet surface) {
    surface.pushMatrix();
    surface.translate(cornerXY.x, cornerXY.y);
  }

  protected void popPosition(PApplet surface) {
    surface.popMatrix();
  }

  protected void pushPadding(PGraphics surface) {
    surface.pushMatrix();
    surface.translate(0 + padding.getL(), 0 + padding.getR());
  }

  protected void popPadding(PGraphics surface) {
    surface.popMatrix();
  }

  protected Dimension calcPaddedDimensions() {
    return new Dimension(w - padding.getL() - padding.getR(), h - padding.getT() - padding.getB());
  }

  protected Point2D.Float calcTopCenterPlacement(Dimension bounds, Dimension toCenter) {
    float x = (bounds.width - toCenter.width) / 2f;
    float y = 0;
    return new Point2D.Float(x, y);
  }

  protected Point2D.Float calcBottomCenterPlacement(Dimension bounds, Dimension toCenter) {
    float x = (bounds.width - toCenter.width) / 2f;
    float y = bounds.height - toCenter.height;
    return new Point2D.Float(x, y);
  }

  protected Point2D.Float calcLeftCenterPlacement(Dimension bounds, Dimension toCenter) {
    float x = 0;
    float y = (bounds.height - toCenter.height) / 2f;
    return new Point2D.Float(x, y);
  }

  protected Point2D.Float calcRightCenterPlacement(Dimension bounds, Dimension toCenter) {
    float x = bounds.width - toCenter.width;
    float y = (bounds.height - toCenter.height) / 2f;
    return new Point2D.Float(x, y);
  }

  /**
   * Draws a rectangle stroke boundary onto the surface, at the origin.
   * @param surface
   */
  protected void drawBoundaries(PApplet surface) {
    surface.pushStyle();
    surface.rectMode(PConstants.CORNER);
    surface.strokeWeight(2);
    surface.stroke(200);
    surface.fill(255, 10);
    surface.rect(0, 0, w, h);
    surface.popStyle();
  }

  @Override
  public String getWidgetID() {
    return widgetID;
  }

  @Override
  public void enableHoverTooltip(boolean enable) {
    this.showTooltip = enable;
  }
}
