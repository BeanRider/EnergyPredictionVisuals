package mvc.model.widgets.widgetBuilder;

import mvc.model.datasource.TemporalNumberDS;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.GeoSnapShotWidgetModel;

import java.awt.geom.Point2D;
import java.util.Map;

public class GeoSnapShotWidgetModelBuilder {

  private TimeRange rangeToDisplay;
  private Map<Integer, TemporalNumberDS<Float>> data;
  private Point2D.Float originLatLong;

  public GeoSnapShotWidgetModel build() {
    if (rangeToDisplay == null || data == null) {
      throw new NullPointerException("Build failed, please set up all fields!");
    }

    return new GeoSnapShotWidgetModel(rangeToDisplay, originLatLong, data);
  }

  public GeoSnapShotWidgetModelBuilder setRange(TimeRange rangeToDisplay) {
    this.rangeToDisplay = rangeToDisplay;
    return this;
  }

  public GeoSnapShotWidgetModelBuilder setDataSource(Map<Integer, TemporalNumberDS<Float>> dataSource) {
    this.data = dataSource;
    return this;
  }

  public GeoSnapShotWidgetModelBuilder setOriginLatLong(Point2D.Float originLatLong) {
    this.originLatLong = originLatLong;
    return this;
  }
}

