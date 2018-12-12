package mvc.view;

/**
 * Created by jeffrey02px2014 on 10/16/16.
 */
public class Margins extends AbstractBoxValues {

  public Margins(int l, int r, int t, int b) {
    super(l, r, t, b);
  }

  public Margins(int value) {
    super(value, value, value, value);
  }
}
