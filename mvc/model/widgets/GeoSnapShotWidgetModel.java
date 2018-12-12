package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.datasource.TemporalNumberDS;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.widgetBuilder.GeoSnapShotWidgetModelBuilder;
import visitors.widgetmodel.IWidgetModelVisitor;

import java.awt.geom.Point2D;
import java.util.Map;

/**
 * Created by jeffrey02px2014 on 10/19/16.
 */
public class GeoSnapShotWidgetModel implements IWidgetModel {

  private TimeRange rangeToDisplay;
  private Map<Integer, TemporalNumberDS<Float>> data;
  private Point2D.Float originLatLong;

  public GeoSnapShotWidgetModel(TimeRange rangeToDisplay, Point2D.Float originLatLong, Map<Integer, TemporalNumberDS<Float>> data) {
    this.rangeToDisplay = rangeToDisplay;
    this.data = data;
    this.originLatLong = originLatLong;
  }

  @Override
  public TimeRange getTimeInformation() {
    return rangeToDisplay.clone();
  }

  @Override
  public WidgetType getWidgetType() {
    return WidgetType.GeoSnapShot;
  }

  @Override
  public DataSource shallowCopyData() {
    return null; // FIXME
  }

  public Map<Integer, TemporalNumberDS<Float>> getData() {
    return data;
  }

  public Point2D.Float getOriginLatLong() {
    return originLatLong;
  }
  /**
   * WARNING: makes a shallow copy of the data!
   */
  @Override
  public IWidgetModel clone() {
    return new GeoSnapShotWidgetModelBuilder()
            .setDataSource(data)
            .setOriginLatLong(originLatLong)
            .setRange(rangeToDisplay.clone())
            .build();
  }

  @Override
  public void accept(IWidgetModelVisitor wmv) {
    wmv.visit(this);
  }


}

