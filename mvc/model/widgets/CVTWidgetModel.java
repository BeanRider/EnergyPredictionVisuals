package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.dimension.time.TimeRange;
import visitors.widgetmodel.IWidgetModelVisitor;

/**
 * Created by redbeans on 12/7/16.
 */
public class CVTWidgetModel extends AbstractTemporalWM {

    public CVTWidgetModel(TimeRange rangeToDisplay) {
        super(rangeToDisplay);
    }

    @Override
    public WidgetType getWidgetType() {
        return WidgetType.CVT;
    }

    @Override
    public DataSource shallowCopyData() {
        return null;
    }

    @Override
    public IWidgetModel clone() {
        return null;
    }

    @Override
    public void accept(IWidgetModelVisitor wmv) {
        wmv.visit(this);
    }
}
