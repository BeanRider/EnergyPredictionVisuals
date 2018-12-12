package mvc.controller;

import mvc.model.CityVisualizationModel;
import mvc.view.CityVisualizationView;

/**
 * Created by jeffrey02px2014 on 9/14/16.
 */
public interface CityVisualizationController {

  void init(CityVisualizationView v, CityVisualizationModel m);

  void initView(CityVisualizationView rcpVisualizer_dynamicView);

  void tick();
}
