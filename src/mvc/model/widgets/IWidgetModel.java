package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.dimension.time.TimeRange;
import visitors.widgetmodel.IWidgetModelVisitor;

/**
 * Created by jeffrey02px2014 on 10/19/16.
 */
public interface IWidgetModel {
  TimeRange getTimeInformation();

  WidgetType getWidgetType();

  /**
   * Warning: does not make a copy of this variable!
   * @return
   */
  DataSource shallowCopyData();

  enum WidgetType {
    TimeSeries, GeoSnapShot, CVT, Treemap, Violin, Cluster
  }

  IWidgetModel clone();

  void accept(IWidgetModelVisitor wmv);
}
