package mvc.view.widgets;

import mvc.model.widgets.IWidgetModel;
import visitors.widgetview.IWidgetViewVisitor;
import mvc.view.Interactable;

/**
 * Interface for all standard visualization widgets
 */
public interface IWidgetView extends Interactable {
  String getWidgetID();

  void enableHoverTooltip(boolean enable);

  IWidgetModel.WidgetType getWidgetType();

  void accept(IWidgetViewVisitor visitor);
}
