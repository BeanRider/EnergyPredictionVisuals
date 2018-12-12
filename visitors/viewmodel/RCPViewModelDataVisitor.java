package visitors.viewmodel;

import mvc.model.datasource.IData;
import mvc.view.viewmodel.RCPVisualizer_ViewModel;

import java.util.Optional;

/**
 * Created by redbeans on 12/7/16.
 */
public class RCPViewModelDataVisitor implements ICityVisualizationViewModelVisitor{
    public Optional<String> request = Optional.empty();
    public Optional<IData> response = Optional.empty();
    public RCPViewModelDataVisitor(String request) {
        this.request = Optional.of(request);
    }

    @Override
    public void visit(RCPVisualizer_ViewModel vm) {
        if (request.isPresent()) {
            System.out.println(request.get());
            response = Optional.of(vm.requestData(request.get()));
        } else {
            throw new RuntimeException("Please pass a request to the view model!");
        }
    }

    public IData getResponse() {
        if (response.isPresent()) {
            return response.get();
        }
        throw new RuntimeException("No getResponse!");
    }
}
