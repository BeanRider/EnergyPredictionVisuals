package utility.voro;

import processing.core.PApplet;
import static utility.voro.Global.distance_tolerance;

public class Voronoi {

    private Bisector default_bisector;

    Cell[] cells = new Cell[1000];
    int ncells = 0;
    Polygon boundary;

    public Voronoi(Point[] $points, Polygon $boundary) {
        boundary = $boundary.get_convex_hull();
        Point p1 = new Point(-9999, -9999, 0);
        Point p2 = new Point(-9999, -9998, 0);
        default_bisector = new Bisector(p1, p2);
        for (Point i : $points) {
            add_cell(i);
        }
    }

    public Point[] getAllCellCentroids() {
        Point[] centroids = new Point[ncells];
        for (int i = 0; i < ncells; ++i) {
            Point centroid = cells[i].boundary.get_centroid_point();
            centroids[i] = new Point(
                    centroid.x,
                    centroid.y,
                    centroid.z);
        }
        return centroids;
    }

    void add_cell(Point $p) {

        // Ignore added cell if distance from $p to any current cells is < distance_tolerance
        for (int i = 0; i < ncells; i++) {
            if ($p.get_distance_to(cells[i].p.x, cells[i].p.y, cells[i].p.z) < distance_tolerance) {
                return;
            }
        }

        Cell alpha = new Cell($p);
        cells[ncells] = alpha;
        ncells++;

        Cell[] cs = {alpha};

        if (ncells == 1) {
            // First cell
            Subcell s = new Subcell(boundary);
            s.c = alpha;
            alpha.subcells[0] = s;
            alpha.nsubcells++;
        } else {
            // Saves the type of intersection for a cell.
            String[][] directions = new String[1000][100];

            // Saves all polygons during splitting
            Polygon[][][] polygons = new Polygon[1000][100][2];

            // Saves all current subcells of each cell
            Subcell[][] subcells = new Subcell[1000][100];

            // Saves all bisectors from adding a new cell with point $p
            Bisector[] bisectors = new Bisector[1000];

            boolean skippers[] = new boolean[1000];

            // for each of every cell before the new cell alpha...
            for (int i = 0; i < ncells - 1; i++) {

                // form a bisector b (previous cell n's point, alpha's point)
                Bisector b = new Bisector(cells[i].p, alpha.p);
                bisectors[i] = b;
                skippers[i] = true;

                // check if any of cell n's subcells can be split by that bisector b.
                for (int j = 0; j < cells[i].nsubcells; j++) {
                    subcells[i][j] = cells[i].subcells[j]; // save every cell's subcells no matter what.
                    if (cells[i].subcells[j].boundary.is_splittable(b.l)) {
                        polygons[i][j] = cells[i].subcells[j].boundary.split(b.l);
                        directions[i][j] = "split";
                        skippers[i] = false;
                    } else {
                        directions[i][j] = "associate";
                    }
                }
            }
            for (int i = 0; i < ncells - 1; i++) {
                //if none of this cell's subcells are affected, skip it
                if (skippers[i]) {
                    continue;
                }
                int nsubcells = cells[i].nsubcells;
                cells[i].nsubcells = 0;
                cells[i].consolidated = false;
                cs = new Cell[2];
                cs[0] = alpha;
                cs[1] = cells[i];
                for (int j = 0; j < nsubcells; j++) {
                    if (directions[i][j] == "associate") {
                        //associate subcells whose cells are affected but are not split
                        subcells[i][j].associate(cs, bisectors[i], true);
                    } else if (directions[i][j] == "split") {
                        //replace split subcells
                        cells[i].remove_subcell(subcells[i][j]);
                        Subcell s1 = new Subcell(polygons[i][j][0]);
                        s1.associate(cs, bisectors[i], false);
                        Subcell s2 = new Subcell(polygons[i][j][1]);
                        s2.associate(cs, bisectors[i], false);
                    }
                }
            }
            for (int i = 0; i < ncells; i++) {
                cells[i].consolidate();
            }
        }
    }

    /**
     * Return the voronoi cell containing point (x, y).
     * @param $x
     * @param $y
     * @return
     */
    Cell get_containing_cell(float $x, float $y) {
        Cell c = cells[0];
        for (int i = 0; i < ncells; i++) {
            for (int j = 0; j < cells[i].nsubcells; j++) {
                if (cells[i].subcells[j].boundary.is_coord_inside($x, $y, true)) {
                    c = cells[i];
                    return c;
                }
            }
        }
        return c;
    }

    public void render(PApplet surface) {
        for (int i = 0; i < ncells; i++) {
            cells[i].render(surface);
        }
    }
    public void render2D(PApplet surface) {
        for (int i = 0; i < ncells; i++) {
            cells[i].render2D(surface);
        }
    }
}
