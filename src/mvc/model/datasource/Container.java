package mvc.model.datasource;

/**
 * Created by redbeans on 12/7/16.
 */
public class Container<T> implements IData<T> {

    private T data;

    public Container(T data) {
        this.data = data;
    }

    @Override
    public void accept(IDataVisitor dv) {
        dv.visit(this);
    }

    @Override
    public T unbox() {
        return data;
    }
}
