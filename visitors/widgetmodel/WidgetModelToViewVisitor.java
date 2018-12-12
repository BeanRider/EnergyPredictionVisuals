package visitors.widgetmodel;

import mvc.model.widgets.*;
import mvc.view.widgets.*;

import java.util.List;
import java.util.Optional;

/**
 * A WidgetModel visitor that convert a WidgetModel to a WidgetView, and appends it to the package
 */
public class WidgetModelToViewVisitor implements IWidgetModelVisitor {
    private Optional<List<IWidgetView>> toAppend = Optional.empty();

    public WidgetModelToViewVisitor(List<IWidgetView> viewList) {
        toAppend = Optional.of(viewList);
    }

    @Override
    public void visit(GeoSnapShotWidgetModel v) {
        if (toAppend.isPresent()) {
            toAppend.get().add(
                    new GeoSnapShotWidgetView(
                            v.getTimeInformation(),
                            v.getOriginLatLong(),
                            v.getData()));
        }
    }

    @Override
    public void visit(TreeMapWidgetModel v) {
        if (toAppend.isPresent()) {
            toAppend.get().add(
                    new TreeMapWidgetView(v.getData(), v.getTimeInformation()));
        }
    }

    @Override
    public void visit(TimeSeriesWidgetModel v) {
        if (toAppend.isPresent()) {
            toAppend.get().add(
                    new TimeSeriesWidgetView(v.getTimeInformation(), v.shallowCopyData()));
        }
    }

    @Override
    public void visit(CVTWidgetModel cvtWidgetModel) {
        if (toAppend.isPresent()) {
            toAppend.get().add(
                    new CVTWidgetView());
        }
    }

    @Override
    public void visit(ViolinPlotWidgetModel violinWidgetModel) {
        if (toAppend.isPresent()) {
            toAppend.get().add(
                    new ViolinPlotWidgetView(violinWidgetModel.getData(), violinWidgetModel.getBase()));
        }
    }

    @Override
    public void visit(ClusterPlotWidget clusterPlotWidget) {
        if (toAppend.isPresent()) {
            toAppend.get().add(
                    new ClusterPlotWidgetView(clusterPlotWidget.getData(), clusterPlotWidget.getBase()));
        }
    }
}
