package visitors.widgetview;

import mvc.view.widgets.*;

/**
 * A WidgetView visitor that performs different functions for each implementation of WidgetView
 */
public interface IWidgetViewVisitor {
    void visit(GeoSnapShotWidgetView v);
    void visit(TreeMapWidgetView v);
    void visit(TimeSeriesWidgetView v);
    void visit(CVTWidgetView v);
    void visit(ViolinPlotWidgetView violinPlotWidgetView);
    void visit(ClusterPlotWidgetView clusterPlotWidgetView);
}
