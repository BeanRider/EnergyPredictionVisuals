package mvc.view.viewmodel;

import mvc.model.datasource.IData;
import visitors.viewmodel.ICityVisualizationViewModelVisitor;

/**
 * Created by redbeans on 12/7/16.
 */
public interface CityVisualizationViewModel {
    void put(String name, IData data);

    IData get(String name);

    void accept(ICityVisualizationViewModelVisitor vmv);
}
