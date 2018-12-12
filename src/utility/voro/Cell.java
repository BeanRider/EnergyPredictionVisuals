package utility.voro;

import processing.core.PApplet;
import java.util.Arrays;
import static processing.core.PApplet.concat;
import static processing.core.PApplet.subset;

public class Cell {
    Point p;
    Subcell[] subcells = new Subcell[100];
    int nsubcells = 0;
    Polygon boundary;
    boolean consolidated = false;

    Cell(Point $p) {
        p = $p;
    }

    /**
     * Removes the specified subcell $s
     * @param $s
     */
    public void remove_subcell(Subcell $s) {
        Subcell[] temp = Arrays.copyOf(subcells, subcells.length);
        int ntemp = nsubcells;
        nsubcells = 0;
        subcells = new Subcell[100];
        for (int i = 0; i < ntemp; i++) {
            if (temp[i] != $s) {
                subcells[nsubcells] = temp[i];
                subcells[nsubcells].c = this;
                nsubcells++;
            }
        }
    }

    /**
     * Simplifies this cell by combining all subcells belonging to this cell into a single polygon.
     * 1. Concats all points from all subcells belonging to this cell
     * 2. Calculate the resulting convex hull polygon
     * 3. Set new boundary == set subcell list contain only == the convex hull polygon
     */
    public void consolidate() {
        if (nsubcells <= 1 && consolidated) {
            return;
        }
        Point[] temp1 = {};
        for (int i = 0; i < nsubcells; i++) {
            Point[] temp2 = (Point[]) subset(subcells[i].boundary.points, 0, subcells[i].boundary.npoints);
            temp1 = (Point[]) concat(temp1, temp2);
        }
        Polygon pointset = new Polygon(temp1);
        boundary = pointset.get_convex_hull();
        nsubcells = 0;
        subcells = new Subcell[100];
        subcells[0] = new Subcell(boundary);
        subcells[0].c = this;
        nsubcells++;
        consolidated = true;
    }

    void render(PApplet surface) {
        for (int i = 0; i < nsubcells; i++) {
            subcells[i].render(surface);
            surface.pushStyle();
            surface.fill(0, 0, 100);
            subcells[i].boundary.get_centroid_point().render(surface);
            surface.popStyle();
        }
        p.render(surface);
    }
    void render2D(PApplet surface) {
        for (int i = 0; i < nsubcells; i++) {
            subcells[i].render2D(surface);
            surface.pushStyle();
            surface.fill(0, 0, 100);
            subcells[i].boundary.get_centroid_point().render2D(surface);
            surface.popStyle();
        }
        p.render2D(surface);
    }
}
