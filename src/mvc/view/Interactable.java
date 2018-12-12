package mvc.view;

import java.awt.*;

import mvc.controller.CityVisualizationController;
import mvc.view.UIAction.ActionSuite;
import mvc.view.viewmodel.CityVisualizationViewModel;
import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * For all selectable UI components:
 * buttons, scroll-panels, draggers ... so on
 */
public interface Interactable {

  /**
   * @return the corner xy position of this component
   */
  Point getCornerXY();

  /**
   * EFFECT: Sets the corner xy position of this component
   * @param newX new corner x
   * @param newY new corner y
   */
  void setCornerXY(int newX, int newY);

  /**
   * EFFECT: Adds to this component's corner xy
   * @param addedX to x component
   * @param addedY to y component
   */
  void addCornerXY(int addedX, int addedY);

  /**
   * Renders this component to the parentView, and caches it as a buffer
   * @param parentView view to to draw this on
   * @param viewDataModel origin of data to interpret into graphics form (influences graphs, button icons, positioning...)
   */
  void render(PApplet parentView, CityVisualizationViewModel viewDataModel);

  /**
   * Renders this component to the parentView using the old buffer
   * @param s
   */
  void draw(PApplet s);

  /**
   * Determines whether the given mouse positions are on top of this component
   * @param mX current x location of cursor
   * @param mY current y location of cursor
   * @return true if mouse if on "this"
   */
  boolean isMouseOver(float mX, float mY);

  /**
   * The logic to perform when the mouse is confirmed to be hovered on "this"
   * @param controller
   * @param event
   */
  void mouseHoverAction(CityVisualizationController controller, MouseEvent event);

  /**
   * Called when the parent's mouse is scrolling; does not check states before performing action.
   * Please check this states, as well as other Interactables' states before calling mouseScrollAction.
   * @param event mouse event
   * @param controller
   * @throws IllegalStateException if the state is not accepted when this is called
   */
  void mouseScrollAction(MouseEvent event, CityVisualizationController controller);

  /**
   * Called when the parent's mouse is clicked (can fail to activate).
   * @param controller
   * @param event
   */
  void mousePressedAction(CityVisualizationController controller, MouseEvent event);

  /**
   * Called when the parent's mouse is pressed, but mouse was out of the acceptable bounds.
   * @param controller
   * @param e
   */
  void mousePressedOutsideAction(CityVisualizationController controller, MouseEvent e);

  /**
   * Called when the parent's mouse is dragged
   *
   * @param controller
   * @param event
   * @throws IllegalStateException if the state is not accepted when this is called
   */
  void mouseDraggedAction(CityVisualizationController controller, MouseEvent event);

  /**
   * Called when the parent's mouse is released
   *
   * @param controller
   * @param event
   * @throws IllegalStateException if the state is not accepted when this is called
   */
  void mouseReleasedAction(CityVisualizationController controller, MouseEvent event);

  boolean getFocused();

  /**
   * EFFECT: Restores all hovered states to normal (un-hovered)
   */
  void removeHoveredStates();

  /**
   * @return the dimensions of this component
   */
  Dimension getDimension();

  /**
   * Mutates this interactable's dimensions
   * @param width
   * @param height
   */
  void setDimension(int width, int height);

  /**
   * Checks the interactable's state before performing an event
   * @throws IllegalStateException if the state is invalid
   */
  void checkState(MouseEvent e);

  void bindAction(ActionSuite a);

  ActionSuite getActionSuite();

  void setHelpText(String helpText);

  void pushHelpTextToController(CityVisualizationController c, Point mousePosition);

  /**
   * EFFECT: Resets to its initial state.
   */
  void resetToDefault();
}
