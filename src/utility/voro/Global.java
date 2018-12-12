package utility.voro;

import processing.core.PApplet;
import static processing.core.PApplet.println;


//------ GLOBAL ------//
//the global class holds all other default classes
//its only purpose is for organization and to imitate actionscript
public class Global {

    public static float distance_tolerance = .0001f;
    public static float slope_tolerance = .001f;
    public static float angle_tolerance = .001f;
    public static float point_speed = .1f;
    public static Point zero_point;
    public static Global global;

    int npoints, nsegments, nvectors, nlines, npolygons, npolylines, nplanes, nfaces;
//    Point[] points = new Point[1000000];
//    Segment[] segments = new Segment[1000000];
//    Vector[] vectors = new Vector[1000000];
//    Line[] lines = new Line[1000000];
//    Polygon[] polygons = new Polygon[100000];
//    Polyline[] polylines = new Polyline[100000];
//    Plane[] planes = new Plane[100000];
//    Face[] faces = new Face[100000];
    public Polygon stage;

    public Global() {
        npoints = nsegments = nvectors = nlines = npolygons = npolylines = nplanes = nfaces = 0;
    }

    public void init(int width, int height) {
        Point[] ps = new Point[4];
        ps[0] = new Point(-width / 2, -height / 2, 0);
        ps[1] = new Point(width / 2, -height / 2, 0);
        ps[2] = new Point(width / 2, height / 2, 0);
        ps[3] = new Point(-width / 2, height / 2, 0);
        stage = new Polygon(ps);
        zero_point = new Point(0, 0, 0);
    }

//    void echo() {
//        println("---- GLOBAL ----");
//        println("Points: " + npoints);
//        println("Segments: " + nsegments);
//        println("Vectors: " + nvectors);
//        println("Lines: " + nlines);
//        println("Polygons: " + npolygons);
//        println("Polylines: " + npolylines);
//        println("Planes: " + nplanes);
//        println("Faces: " + nfaces);
//    }

//    void render(int $n, PApplet surface) {
//        if ($n >= 128) {
//            for (int i = 0; i < nfaces; i++) {
//                surface.fill(255, 240, 240);
//                surface.stroke(255);
//                surface.strokeWeight(1);
//                faces[i].render(surface);
//            }
//            $n -= 128;
//        }
//        if ($n >= 64) {
//            for (int i = 0; i < npolylines; i++) {
//                surface.fill(255, 250, 250);
//                surface.stroke(0);
//                surface.strokeWeight(1);
//                polylines[i].render(surface);
//            }
//            $n -= 64;
//        }
//        if ($n >= 32) {
//            for (int i = 0; i < npolygons; i++) {
//                surface.fill(255, 250, 250);
//                surface.stroke(0);
//                surface.strokeWeight(1);
//                polygons[i].render(surface);
//            }
//            $n -= 32;
//        }
//        if ($n >= 16) {
//            surface.noFill();
//            surface.stroke(0, 0, 255);
//            surface.strokeWeight(1);
//            for (int i = 0; i < nlines; i++) {
//                lines[i].render(surface);
//            }
//            $n -= 16;
//        }
//        if ($n >= 8) {
//            surface.noFill();
//            surface.stroke(0, 255, 255);
//            surface.strokeWeight(1);
//            for (int i = 0; i < nvectors; i++) {
//                vectors[i].render(zero_point, surface);
//            }
//            $n -= 8;
//        }
//        if ($n >= 4) {
//            surface.noFill();
//            surface.stroke(255, 0, 255);
//            surface.strokeWeight(1);
//            for (int i = 0; i < nsegments; i++) {
//                segments[i].render(surface);
//            }
//            $n -= 4;
//        }
//        if ($n >= 2) {
//            surface.fill(0);
//            surface.stroke(0);
//            surface.strokeWeight(1);
//            for (int i = 0; i < npoints; i++) {
//                points[i].render(surface);
//            }
//            $n -= 2;
//        }
//        if ($n >= 1) {
//            surface.noFill();
//            surface.stroke(255, 0, 0);
//            surface.strokeWeight(1);
//            stage.render(surface);
//            $n -= 1;
//        }
//    }
}
