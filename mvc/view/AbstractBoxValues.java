package mvc.view;

/**
 * Created by jeffrey02px2014 on 10/16/16.
 */
public abstract class AbstractBoxValues implements BoxValues {

  private int leftPixels = 0;
  private int rightPixels = 0;
  private int topPixels = 0;
  private int bottomPixels = 0;

  protected AbstractBoxValues(int l, int r, int t, int b) {
    this.leftPixels = l;
    this.rightPixels = r;
    this.topPixels = t;
    this.bottomPixels = b;
  }

  protected AbstractBoxValues(int value) {
    this(value, value, value, value);
  }

  @Override
  public int getL() {
    return leftPixels;
  }

  @Override
  public int getR() {
    return rightPixels;
  }

  @Override
  public int getT() {
    return topPixels;
  }

  @Override
  public int getB() {
    return bottomPixels;
  }

  @Override
  public Padding clone() {
    return new Padding(leftPixels, rightPixels, topPixels, bottomPixels);
  }
}
