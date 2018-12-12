package mvc.model.dimension;

/**
 * Created by jeffrey02px2014 on 10/19/16.
 */
public class ColorUtil {
  public static int color(int v1, int v2, int v3) {
    if(v1 > 255) {
      v1 = 255;
    } else if(v1 < 0) {
      v1 = 0;
    }

    if(v2 > 255) {
      v2 = 255;
    } else if(v2 < 0) {
      v2 = 0;
    }

    if(v3 > 255) {
      v3 = 255;
    } else if(v3 < 0) {
      v3 = 0;
    }

    return -16777216 | v1 << 16 | v2 << 8 | v3;
  }

}
