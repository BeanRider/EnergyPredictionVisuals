package mvc.view.UIAction;

import mvc.controller.CityVisualizationController;
import processing.event.MouseEvent;

/**
 * Created by jeffrey02px2014 on 5/15/16.
 */
public abstract class Action {

  public static final Action DEFAULT = new Action() {
    @Override
    public void act(CityVisualizationController controller, MouseEvent e) {
      // DO NOTING
    }
  };

  public abstract void act(CityVisualizationController controller, MouseEvent e);
}