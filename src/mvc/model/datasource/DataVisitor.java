package mvc.model.datasource;

import java.util.Optional;

/**
 * Created by redbeans on 12/7/16.
 */
public class DataVisitor<T extends IData> implements IDataVisitor<T> {

    private Optional<T> response = Optional.empty();
    @Override
    public void visit(T t) {
        response = Optional.of(t);
    }

    @Override
    public T getResponse() {
        if (response.isPresent()) {
            return response.get();
        }
        throw new RuntimeException("No getResponse!");
    }
}
