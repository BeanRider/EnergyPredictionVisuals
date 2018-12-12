package mvc.view.viewmodel;

import mvc.model.datasource.IData;
import visitors.viewmodel.ICityVisualizationViewModelVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by redbeans on 12/7/16.
 */
public class RCPVisualizer_ViewModel implements CityVisualizationViewModel {
    Map<String, IData> sharedViewData = new HashMap<>();

    @Override
    public void put(String name, IData data) {
        sharedViewData.put(name, data);
    }

    @Override
    public IData get(String name) {
        return sharedViewData.get(name);
    }

    @Override
    public void accept(ICityVisualizationViewModelVisitor vmv) {
        vmv.visit(this);
    }

    public IData requestData(String request) {
        return sharedViewData.get(request);
    }
}
