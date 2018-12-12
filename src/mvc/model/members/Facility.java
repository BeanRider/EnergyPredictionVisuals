package mvc.model.members;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Created by jeffrey02px2014 on 9/14/16.
 */
public interface Facility {

  /**
   * @return the unique int ID of this facility
   */
  int getID();

  /**
   * @return the full name of this facility
   */
  String getName();

  /**
   * @return the String representation of the use type of this facility
   */
  String getUse();

  /**
   * @return # of floors in this facility
   */
  int getFloors();

  /**
   * @return the symbolic color for this facility
   */
  int getColor();

  /**
   * @return the center position of this facility
   */
  Point2D.Float getCenter();

  /**
   * @return the vertices of the outline of this facility
   */
  List<Point2D.Float> getVertices();

  /**
   * @return the sqft area of one floor of this facility
   */
  float getArea();

  /**
   * Make a copy of this facility
   * @return a deep copy
   */
  Facility clone();

}
