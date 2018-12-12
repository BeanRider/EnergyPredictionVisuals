package visitors.widgetview;

import mvc.view.widgets.*;
import processing.core.PApplet;

import java.util.Optional;

/**
 * A WidgetView visitor that renders the time values of a WidgetView, if pertinent.
 */
public class WidgetViewDrawVisitor<R> implements IWidgetViewVisitor {

    Optional<Object> packageArguments = Optional.empty();

    public WidgetViewDrawVisitor(Object renderer) {
        packageArguments = Optional.of(renderer);
    }

    @Override
    public void visit(GeoSnapShotWidgetView v) {
        if (packageArguments.isPresent()) {
            v.draw((PApplet) packageArguments.get());
        }
    }

    @Override
    public void visit(TreeMapWidgetView v) {
        if (packageArguments.isPresent()) {
            v.draw((PApplet) packageArguments.get());
        }
    }

    @Override
    public void visit(TimeSeriesWidgetView v) {
        if (packageArguments.isPresent()) {
            v.draw((PApplet) packageArguments.get());
        }
    }

    @Override
    public void visit(CVTWidgetView v) {
        if (packageArguments.isPresent()) {
            v.draw((PApplet) packageArguments.get());
        }
    }

    @Override
    public void visit(ViolinPlotWidgetView v) {
        if (packageArguments.isPresent()) {
            v.draw((PApplet) packageArguments.get());
        }
    }

    @Override
    public void visit(ClusterPlotWidgetView v) {
        if (packageArguments.isPresent()) {
            v.draw((PApplet) packageArguments.get());
        }
    }
}
