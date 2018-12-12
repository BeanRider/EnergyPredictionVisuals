package mvc.view.widgets;

import mvc.model.widgets.IWidgetModel;
import mvc.view.viewmodel.CityVisualizationViewModel;
import visitors.widgetview.IWidgetViewVisitor;
import processing.core.PApplet;

/**
 * Created by redbeans on 12/7/16.
 */
public class CVTWidgetView extends AbstractBoxWidgetView {
    public CVTWidgetView(int width, int height, String widgetID) {
        super(width, height, widgetID);
    }

    public CVTWidgetView() {
        super(1280, 800, "CVT");
    }

    @Override
    public IWidgetModel.WidgetType getWidgetType() {
        return IWidgetModel.WidgetType.CVT;
    }

    @Override
    public void accept(IWidgetViewVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void render(PApplet parentView, CityVisualizationViewModel viewDataModel) {

    }

    @Override
    public void removeHoveredStates() {

    }

    @Override
    public void resetToDefault() {

    }

    @Override
    public void draw(PApplet s) {

    }
}
