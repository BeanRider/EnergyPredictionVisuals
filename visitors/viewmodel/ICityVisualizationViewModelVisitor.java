package visitors.viewmodel;

import mvc.view.viewmodel.RCPVisualizer_ViewModel;

/**
 * Created by redbeans on 12/7/16.
 */
public interface ICityVisualizationViewModelVisitor {
    void visit(RCPVisualizer_ViewModel vm);
}
