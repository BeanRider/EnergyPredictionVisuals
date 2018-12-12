package visitors.widgetmodel;

import mvc.model.widgets.*;

/**
 * A WidgetModel visitor that performs different functions for each implementation of WidgetModel
 */
public interface IWidgetModelVisitor {
    void visit(GeoSnapShotWidgetModel v);
    void visit(TreeMapWidgetModel v);
    void visit(TimeSeriesWidgetModel v);
    void visit(CVTWidgetModel cvtWidgetModel);
    void visit(ViolinPlotWidgetModel violinWidgetModel);
    void visit(ClusterPlotWidget clusterPlotWidget);
}
