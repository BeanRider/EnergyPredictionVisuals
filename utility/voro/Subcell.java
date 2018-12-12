package utility.voro;

import processing.core.PApplet;

public class Subcell {
    Cell c;
    Polygon boundary;

    Subcell(Polygon $boundary) {
        boundary = $boundary;
    }

    void associate(Cell[] $cells, Bisector $b, boolean $delete) {
        if ($delete) {
            c.remove_subcell(this);
            c.consolidated = false;
        }
        Point center = boundary.get_centroid_point();
        for (int i = 0; i < $cells.length; i++) {
            Segment s = new Segment($cells[i].p, center);
            if (!s.is_line_intersecting($b.l, true)) {
                c = $cells[i];
                c.subcells[c.nsubcells] = this;
                c.nsubcells++;
                return;
            }
        }
    }

    void render(PApplet surface) {
        boundary.render(surface);
    }

    public void render2D(PApplet surface) {
        boundary.render2D(surface);
    }
}
