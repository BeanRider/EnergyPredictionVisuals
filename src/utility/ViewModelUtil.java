package utility;

import mvc.model.datasource.DataVisitor;
import mvc.model.datasource.IData;
import mvc.model.datasource.IDataVisitor;
import mvc.view.viewmodel.CityVisualizationViewModel;
import visitors.viewmodel.RCPViewModelDataVisitor;

/**
 * Created by redbeans on 12/7/16.
 */
public class ViewModelUtil<T> {

    public T getContainer(String req, CityVisualizationViewModel vm) {
        RCPViewModelDataVisitor visitor = new RCPViewModelDataVisitor(req);
        vm.accept(visitor);
        IData data = visitor.getResponse();
        IDataVisitor<mvc.model.datasource.Container<T>> dataVisitor = new DataVisitor<>();
        data.accept(dataVisitor);
        T facilityOutlines = dataVisitor.getResponse().unbox();
        return facilityOutlines;
    }
}
