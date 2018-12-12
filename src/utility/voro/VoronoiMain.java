package utility.voro;

import processing.core.PApplet;
import utility.GraphicsAlgorithms;

import static utility.voro.Global.global;

public class VoronoiMain extends PApplet {

    private Voronoi diagram;

    public void setup() {
        //initialize global
        global = new Global();
        global.init(width, height);

        //initialize voronoi diagram
        Point[] ps = new Point[50];
        for (int i = 0; i < ps.length; i++) {
            ps[i] = new Point(random(-width / 2, width / 2), random(-height / 2, height / 2), 0);
        }
        diagram = new Voronoi(ps, global.stage);
    }

    public void draw() {
        render();
    }

    private void render() {
        background(255);
        pushMatrix();
        translate(width / 2, height / 2, 0);

        stroke(0, 100);
        strokeWeight(1);
        noFill();
        diagram.render(this);

//        Cell selectedCell = diagram.get_containing_cell(mouseX - width / 2, mouseY - height / 2);
//        fill(255, 150, 150, 150);
//        stroke(0);
//        strokeWeight(1);
//        selectedCell.render(this);

        popMatrix();
    }

    public void settings() {
        size(1200, 800, P3D);
    }

    public void mousePressed() {
        for (int i = 0; i < 3000; ++i) {
            diagram = GraphicsAlgorithms.singleLloyds(diagram.getAllCellCentroids(), global.stage);
            System.out.println(i);
        }
        for (Point c : diagram.getAllCellCentroids()) {
            System.out.println(c.x + ", " + c.y);
        }
//        Point p = new Point(mouseX - width / 2, mouseY - height / 2, 0);
//        diagram.add_cell(p);
//        render();
    }
}