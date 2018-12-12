package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.widgetBuilder.TimeSeriesWidgetModelBuilder;
import visitors.widgetmodel.IWidgetModelVisitor;

/**
 * Created by jeffrey02px2014 on 10/19/16.
 */
public class TimeSeriesWidgetModel extends AbstractTemporalWM implements IWidgetModel {

  private DataSource data;

  public TimeSeriesWidgetModel(TimeRange rangeToDisplay, DataSource data) {
    super(rangeToDisplay);
    this.data = data;
  }

  @Override
  public WidgetType getWidgetType() {
    return WidgetType.TimeSeries;
  }

  @Override
  public DataSource shallowCopyData() {
    return data;
  }

  /**
   * WARNING: makes a shallow copy of the data!
   */
  @Override
  public IWidgetModel clone() {
    return new TimeSeriesWidgetModelBuilder()
            .setDataSource(data)
            .setRange(super.rangeToDisplay.clone())
            .build();
  }

  @Override
  public void accept(IWidgetModelVisitor wmv) {
    wmv.visit(this);
  }

}

