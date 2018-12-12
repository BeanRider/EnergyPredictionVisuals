package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.datasource.TemporalNumberDS;
import mvc.model.dimension.time.TimeRange;
import mvc.model.widgets.widgetBuilder.TreemapWidgetModelBuilder;
import visitors.widgetmodel.IWidgetModelVisitor;

import java.util.List;

/**
 * Created by redbeans on 12/1/16.
 */
public class TreeMapWidgetModel extends AbstractTemporalWM implements IWidgetModel {

    private List<TemporalNumberDS<Float>> data;

    public TreeMapWidgetModel(TimeRange rangeToDisplay, List<TemporalNumberDS<Float>> data) {
        super(rangeToDisplay);
        this.data = data;
    }

    @Override
    public IWidgetModel.WidgetType getWidgetType() {
        return IWidgetModel.WidgetType.Treemap;
    }

    @Override
    public DataSource shallowCopyData() {
        return null; // FIXME
    }

    public List<TemporalNumberDS<Float>> getData() {
        return data;
    }

    /**
     * WARNING: makes a shallow copy of the data!
     */
    @Override
    public IWidgetModel clone() {
        return new TreemapWidgetModelBuilder()
                .setDataSource(data)
                .setRange(super.rangeToDisplay.clone())
                .build();
    }

    @Override
    public void accept(IWidgetModelVisitor wmv) {
        wmv.visit(this);
    }

}
