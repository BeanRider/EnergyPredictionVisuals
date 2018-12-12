package mvc.model.datasource;

/**
 * Created by redbeans on 12/7/16.
 */
public interface IData<T> {
    void accept(IDataVisitor dv);

    T unbox();
}
