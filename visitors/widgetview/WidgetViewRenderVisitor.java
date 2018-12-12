package visitors.widgetview;

import mvc.view.viewmodel.CityVisualizationViewModel;
import mvc.view.widgets.*;
import processing.core.PApplet;

import java.util.Optional;

/**
 * A WidgetView visitor that renders the time values of a WidgetView, if pertinent.
 */
public class WidgetViewRenderVisitor implements IWidgetViewVisitor {

    Optional<PApplet> renderSurface = Optional.empty();
    Optional<CityVisualizationViewModel> vm = Optional.empty();

    public WidgetViewRenderVisitor(PApplet renderer, CityVisualizationViewModel vm) {
        renderSurface = Optional.of(renderer);
        this.vm = Optional.of(vm);
    }

    @Override
    public void visit(GeoSnapShotWidgetView v) {
        if (renderSurface.isPresent() && vm.isPresent()) {
            v.render(renderSurface.get(), vm.get());
        }
    }

    @Override
    public void visit(TreeMapWidgetView v) {
        if (renderSurface.isPresent() && vm.isPresent()) {
            v.render(renderSurface.get(), vm.get() );
        }
    }

    @Override
    public void visit(TimeSeriesWidgetView v) {
        if (renderSurface.isPresent() && vm.isPresent()) {
            v.render(renderSurface.get(), vm.get());
        }
    }

    @Override
    public void visit(CVTWidgetView v) {
        if (renderSurface.isPresent() && vm.isPresent()) {
            v.render(renderSurface.get(), vm.get());
        }
    }

    @Override
    public void visit(ViolinPlotWidgetView v) {
        if (renderSurface.isPresent() && vm.isPresent()) {
            v.render(renderSurface.get(), vm.get());
        }
    }

    @Override
    public void visit(ClusterPlotWidgetView v) {
        if (renderSurface.isPresent() && vm.isPresent()) {
            v.render(renderSurface.get(), vm.get());
        }
    }
}
