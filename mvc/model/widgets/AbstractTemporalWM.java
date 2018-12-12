package mvc.model.widgets;

import mvc.model.datasource.DataSource;
import mvc.model.dimension.time.TimeRange;
import visitors.widgetmodel.IWidgetModelVisitor;

import java.util.Objects;

/**
 * Created by redbeans on 12/7/16.
 */
public abstract class AbstractTemporalWM extends AbstractVM implements IWidgetModel {

    protected TimeRange rangeToDisplay;

    public AbstractTemporalWM(TimeRange rangeToDisplay) {
        Objects.requireNonNull(rangeToDisplay);
        this.rangeToDisplay = rangeToDisplay;
    }

    @Override
    public TimeRange getTimeInformation() {
        return rangeToDisplay.clone();
    }

    @Override
    public abstract WidgetType getWidgetType();

    @Override
    public abstract DataSource shallowCopyData();

    @Override
    public abstract IWidgetModel clone();

    @Override
    public abstract void accept(IWidgetModelVisitor wmv);
}
