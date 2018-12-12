package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.datasource.TemporalNumberDS;
import mvc.model.dimension.time.TimeRange;
import visitors.widgetmodel.IWidgetModelVisitor;
import java.util.Map;

/**
 * Created by redbeans on 1/6/17.
 */
public class ClusterPlotWidget extends AbstractVM {
    private Map<Integer, TemporalNumberDS<Float>> data;
    private Map<Integer, TemporalNumberDS<Float>> base;

    public ClusterPlotWidget(Map<Integer, TemporalNumberDS<Float>> data,
                             Map<Integer, TemporalNumberDS<Float>> base) {
        this.data = data;
        this.base = base;
    }

    @Override
    public TimeRange getTimeInformation() {
        return null;
    }

    @Override
    public WidgetType getWidgetType() {
        return WidgetType.Cluster;
    }

    @Override
    public DataSource shallowCopyData() {
        return null;
    }

    @Override
    public IWidgetModel clone() {
        // TODO This is a shallow copy.
        return new ClusterPlotWidget(data, base);
    }

    @Override
    public void accept(IWidgetModelVisitor wmv) {
        wmv.visit(this);
    }

    public Map<Integer, TemporalNumberDS<Float>> getData() {
        return data;
    }

    public Map<Integer, TemporalNumberDS<Float>> getBase() {
        return base;
    }

    public static class Builder {

        private Map<Integer, TemporalNumberDS<Float>> data;
        private Map<Integer, TemporalNumberDS<Float>> baseData;

        public ClusterPlotWidget build() {
            if (data == null) {
                throw new NullPointerException("Build failed, please set up all fields!");
            }
            return new ClusterPlotWidget(data, baseData);
        }

        public Builder setDataSource(Map<Integer, TemporalNumberDS<Float>> dataSource, Map<Integer, TemporalNumberDS<Float>> base) {
            this.data = dataSource;
            this.baseData = base;
            return this;
        }
    }
}