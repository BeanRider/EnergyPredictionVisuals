package visitors.widgetview;

import mvc.view.widgets.*;
import org.joda.time.DateTime;

import java.util.Optional;

/**
 * A WidgetView visitor that updates the time values of a WidgetView, if pertinent.
 */
public class WidgetViewUpdateVisitor implements IWidgetViewVisitor {

    Optional<Object> packageArguments = Optional.empty();

    public WidgetViewUpdateVisitor(Object deliverable) {
        packageArguments = Optional.of(deliverable);
    }

    @Override
    public void visit(GeoSnapShotWidgetView v) {
        if (packageArguments.isPresent()) {
            v.updateTime((DateTime) packageArguments.get());
        }
    }

    @Override
    public void visit(TreeMapWidgetView v) {
        if (packageArguments.isPresent()) {
            v.updateTime((DateTime) packageArguments.get());
        }
    }

    @Override
    public void visit(TimeSeriesWidgetView v) {
        // DO NOTHING
    }

    @Override
    public void visit(CVTWidgetView v) {
        // DO NOTHING
    }

    @Override
    public void visit(ViolinPlotWidgetView violinPlotWidgetView) {
        // DO NOTHING
    }

    @Override
    public void visit(ClusterPlotWidgetView clusterPlotWidgetView) {
        // DO NOTHING
    }
}
