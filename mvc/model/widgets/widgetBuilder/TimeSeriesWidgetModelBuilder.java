package mvc.model.widgets.widgetBuilder;

import mvc.model.datasource.DataSource;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.TimeSeriesWidgetModel;

public class TimeSeriesWidgetModelBuilder {

  private TimeRange rangeToDisplay;
  private DataSource data;

  public TimeSeriesWidgetModel build() {
    if (rangeToDisplay == null || data == null) {
      throw new NullPointerException("Build failed, please set up all fields!");
    }

    return new TimeSeriesWidgetModel(rangeToDisplay, data);
  }

  public TimeSeriesWidgetModelBuilder setRange(TimeRange rangeToDisplay) {
    this.rangeToDisplay = rangeToDisplay;
    return this;
  }

  public TimeSeriesWidgetModelBuilder setDataSource(DataSource dataSource) {
    this.data = dataSource;
    return this;
  }
}

