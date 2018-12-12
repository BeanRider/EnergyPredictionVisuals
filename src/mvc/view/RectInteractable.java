package mvc.view;

import java.awt.*;
import java.util.Objects;

import mvc.controller.CityVisualizationController;
import mvc.view.viewmodel.CityVisualizationViewModel;
import processing.core.PApplet;
import processing.event.MouseEvent;
import mvc.view.UIAction.ActionSuite;

/**
 * Represents a selectable UI component, specifically rect shaped for mouse detection.
 */
public abstract class RectInteractable implements Interactable {

  protected Point cornerXY = new Point(0, 0);
  protected int w, h;
  protected ActionSuite actionSuite = ActionSuite.DEFAULT;

  protected RectInteractable(int width, int height) {
    this.w = width;
    this.h = height;
  }

  @Override
  public void setCornerXY(int newX, int newY) {
    cornerXY = new Point(newX, newY);
  }

  @Override
  public Point getCornerXY() {
    return cornerXY;
  }

  @Override
  public void addCornerXY(int addedX, int addedY) {
    cornerXY = new Point(cornerXY.x + addedX, cornerXY.y + addedY);
  }

  @Override
  public abstract void render(PApplet parentView, CityVisualizationViewModel viewDataModel);

  @Override
  public boolean isMouseOver(float mX, float mY) {
    return cornerXY.x <= mX && mX <= cornerXY.x + w
            && cornerXY.y <= mY && mY <= cornerXY.y + h;
  }

  @Override
  public void mouseHoverAction(CityVisualizationController controller, MouseEvent event) {
    checkState(event);
    pushHelpTextToController(controller, new Point(event.getX(), event.getY()));
    actionSuite.actHovered(controller, event);
  }

  @Override
  public void mousePressedAction(CityVisualizationController controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    checkState(event);
    actionSuite.actPressed(controller, event);
  }

  @Override
  public void mousePressedOutsideAction(CityVisualizationController controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    actionSuite.actPressedOutside(controller, event);
  }

  @Override
  public abstract void removeHoveredStates();

  @Override
  public Dimension getDimension() {
    return new Dimension(w, h);
  }

  @Override
  public void setDimension(int w, int h) {
    this.w = w;
    this.h = h;
  }
  /**
   * By default, this will check the state, but DO NOTHING.
   * @param controller
   * @param event
   */
  @Override
  public void mouseDraggedAction(CityVisualizationController controller, MouseEvent event) {
    // DO NOTHING
  }

  /**
   * By default, this will check the state, but DO NOTHING
   * @param event mouse event
   * @param controller
   */
  @Override
  public void mouseScrollAction(MouseEvent event, CityVisualizationController controller) {
    checkState(event);
    actionSuite.actScrolled(controller, event);
  }

  /**
   * By default, this will check the state, but DO NOTHING
   * @param controller
   * @param event mouse event
   */
  @Override
  public void mouseReleasedAction(CityVisualizationController controller, MouseEvent event) {
    // DO NOTHING
  }

  @Override
  public void checkState(MouseEvent event) {
    if (!isMouseOver(event.getX(), event.getY())) {
      throw new IllegalStateException("You didn't check for this RectInteractable's state before activation!");
    }
  }

  @Override
  public boolean getFocused() {
    // DEFAULT RETURN FALSE
    return false;
  }

  @Override
  public void bindAction(ActionSuite a) {
    this.actionSuite = a;
  }

  @Override
  public ActionSuite getActionSuite() {
    return this.actionSuite;
  }

  private String helpText = "";

  @Override
  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

  @Override
  public void pushHelpTextToController(CityVisualizationController controller, Point mousePosition) {
//    controller.setHelperText(helpText);
  }

}
