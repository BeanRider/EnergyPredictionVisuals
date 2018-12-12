package utility;

import processing.core.PApplet;
import processing.core.PShape;
import utility.voro.Point;
import utility.voro.Polygon;
import utility.voro.Voronoi;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Contains static methods that outputs calculations and rendering results related to graphics.
 */
public class GraphicsAlgorithms {
  public static PShape buildPolygon(List<Point2D.Float> vertices, int color, PApplet engine) {
    engine.color(0, 0, 0);
    // Make an outline of this particular shape
    PShape outline = engine.createShape();
    outline.beginShape();
    outline.fill(color, 40);
//    outline.fill(172, 228, 232, 40);
    outline.strokeWeight(1.5f);
    outline.stroke(color);
//    outline.stroke(172, 228, 232);
    for (Point2D.Float s: vertices) {
      outline.vertex(s.x, s.y); // adds adjustment to keep it centered
    }
    outline.endShape(PApplet.CLOSE);
    return outline;
  }

  /**
   *
   * @param z - initial generators (centroids)
   * @return
   */
  public static Voronoi lloyds(Point[] z, Polygon bounds) {
    // Voronoi Region, V
    Voronoi v = new Voronoi(z, bounds);
    Point[] generators;

    int iterations = 1;
    for (int i = 0; i < iterations; ++i) {
      // Lloyd Iteration
      // Reconstruct centroid regions using LLoyd maps, T, such that z sub (k + 1) = T(zk)
      generators = v.getAllCellCentroids();

      // Reconstruct voronoi regions using V = getRegions(z)
      v = new Voronoi(generators, bounds);
    }
    return v;
  }

  /**
   * option 2:
   * place grid on top of points,
   * calculate num cells that has points in them,
   * increase/decrease grid size, repeat above until cells = num points
   * assign cells to buildings
   */

  public static Voronoi singleLloyds(Point[] z, Polygon bounds) {
    // Voronoi Region, V
    Voronoi v = new Voronoi(z, bounds);
    return v;
  }

}
