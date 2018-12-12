package mvc.model.widgets.widgetBuilder;

import mvc.model.datasource.TemporalNumberDS;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.TreeMapWidgetModel;

import java.util.List;

/**
 * Created by redbeans on 12/1/16.
 */
public class TreemapWidgetModelBuilder {

    private TimeRange rangeToDisplay;
    private List<TemporalNumberDS<Float>> data;

    public TreeMapWidgetModel build() {
        if (rangeToDisplay == null || data == null) {
            throw new NullPointerException("Build failed, please set up all fields!");
        }

        return new TreeMapWidgetModel(rangeToDisplay, data);
    }

    public TreemapWidgetModelBuilder setRange(TimeRange rangeToDisplay) {
        this.rangeToDisplay = rangeToDisplay;
        return this;
    }

    public TreemapWidgetModelBuilder setDataSource(List<TemporalNumberDS<Float>> dataSource) {
        this.data = dataSource;
        return this;
    }
}
