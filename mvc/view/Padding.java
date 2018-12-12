package mvc.view;

/**
 * Created by jeffrey02px2014 on 10/16/16.
 */
public class Padding extends AbstractBoxValues {

  public Padding(int l, int r, int t, int b) {
    super(l, r, t, b);
  }

  public Padding(int value) {
    super(value, value, value, value);
  }

}
