package utility;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains all utilities methods used for String parsing
 */
public class StringParseUtil {

  /**
   * Converts a String with long/lat into a Point2D of (lat/long)
   * @param centroidLongLat - String
   * @return Point2D.Float - as lat/long
   */
  public static Point2D.Float parseCentroidString(String centroidLongLat) {
    List<Float> floatLongLat = getFloatFromSet(centroidLongLat);
    float latitude = floatLongLat.get(1); // flip the given centroid
    float longitude = floatLongLat.get(0); // flip the given centroid order
    //println("lat = " + Float.toString(latitude) + "; long = " + Float.toString(longitude));
    return new Point2D.Float(latitude, longitude);
  }

  /**
   * Parses the given set (MUST BE TWO LEVEL DEEP) into sets of Floats, only one level deeper
   * @param set- must be surrounded by '[' and ']', and have distinct values separated by ','.
   * ONLY USE FOR TWO LEVEL DEEP STRING ARRAY
   */
  public static List<Float> getFloatFromSet(String set) {

    Objects.requireNonNull(set);

    List<Float> resultValues = new ArrayList<>();
    String currentString = set;

    // 1. Remove '['
    currentString = set.substring(1);

    int startI = 0;
    // 2. Run through "currentString"
    for (int i = 0; i < currentString.length(); ++i) {

      char now = currentString.charAt(i);
      // Found a nest, start capturing this nest without going deeper
      if (now == '[') {
        startI = i;
        // finding ending ']'
        for (int s = i; s < currentString.length(); ++s) {
          if (currentString.charAt(s) == ']') {
            resultValues.add(Float.parseFloat(currentString.substring(startI, s + 1).trim()));
            i = s + 2; // set main loop to the index two after the '],'
            startI = s + 2; // set next startingIndex for substring
            break;
          }
        }
      }
      // if current character is a ','; extract
      else if (now == ',') {
        // Add it to the list of floats
        resultValues.add(Float.parseFloat(currentString.substring(startI, i).trim()));
        startI = i + 1;
      }
      // if current character is a ']'; extract final
      else if (now == ']' && i == currentString.length() - 1) {
        // Last value; excluding the ']'
        String lastValue = currentString.substring(startI, currentString.length() - 1).trim();
        resultValues.add(Float.parseFloat(lastValue));
        break;
      }
    }

    //String[] resultArr = resultValues.toArray(new String[resultValues.size()]);
    return resultValues;
  }

  /**
   * Parses the given set (MUST BE TWO LEVEL DEEP) into sets of Strings, only one level deeper
   * @param set must be surrounded by '[' and ']', and have distinct values separated by ','.
   * ONLY USE FOR TWO LEVEL DEEP STRING ARRAY
   */
  public static List<String> getListFromSet(String set) {

    Objects.requireNonNull(set);

    List<String> resultValues = new ArrayList<String>();
    String currentString = set;

    // 1. Remove '['
    currentString = set.substring(1);

    int startI = 0;
    // 2. Run through "currentString"
    for (int i = 0; i < currentString.length(); ++i) {

      char now = currentString.charAt(i);
      // Found a nest, start capturing this nest without going deeper
      if (now == '[') {
        startI = i;
        // finding ending ']'
        for (int s = i; s < currentString.length(); ++s) {
          if (currentString.charAt(s) == ']') {
            resultValues.add(currentString.substring(startI, s + 1).trim());
            i = s + 2; // set main loop to the index two after the '],'
            startI = s + 2; // set next startingIndex for substring
            break;
          }
        }
      }
      // if current character is a ','; extract
      else if (now == ',') {
        // Add it to the list of floats
        resultValues.add(currentString.substring(startI, i).trim());
        startI = i + 1;
      }
      // if current character is a ']'; extract final
      else if (now == ']' && i == currentString.length() - 1) {
        // Last value; excluding the ']'
        String lastValue = currentString.substring(startI, currentString.length() - 1).trim();
        resultValues.add(lastValue);
        break;
      }
    }

    //String[] resultArr = resultValues.toArray(new String[resultValues.size()]);
    return resultValues;
  }
}
