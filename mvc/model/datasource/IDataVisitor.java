package mvc.model.datasource;

/**
 * Created by redbeans on 12/7/16.
 */
public interface IDataVisitor<T extends IData> {
    void visit(T t);
    T getResponse();
}
