package mvc.view;

import mvc.controller.CityVisualizationController;
import mvc.model.datasource.IData;
import mvc.view.widgets.IWidgetView;

import java.awt.*;
import java.util.List;


/**
 * Created by jeffrey02px2014 on 9/14/16.
 */
public interface CityVisualizationView {

  void init(CityVisualizationController c);

  void run();

  Dimension getDimension();

  /**
   * Updates this view's geographical map visuals for each facility.
   * Re-initializes all related variables
   * Does not modify given facilityList.
   * @param name facility graphics passed by the controller
   * @param data
   */
  void updateViewModel(String name, IData data);

  /**
   * Updates this view's time visuals using given time range and current time.
   * Re-initializes all related variables.
   * @param startTime unix-seconds startTime
   * @param currentTime unix-seconds currentTime
   * @param endTime unix-seconds endTime
   */
  void updateAppTimeVisuals(long startTime, long currentTime, long endTime);

  /**
   * Updates this view's list of widget views using the given list of widget views
   * Does not make a copy of the widgets, and uses the references directly.
   * @param iWidgetViews
   */
  void addWidgetVisuals(List<IWidgetView> iWidgetViews);
}
