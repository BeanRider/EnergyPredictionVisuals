package utility.voro;


import processing.core.PApplet;

import static processing.core.PApplet.println;

public class Polyline {
    Segment[] segments = new Segment[1000];
    int nsegments = 0;

    Polyline(Segment[] $segments) {
        segments = $segments;
        nsegments = segments.length;
    }

    float get_length() {
        float l = 0;
        for (int i = 0; i < nsegments; i++) {
            l += segments[i].get_length();
        }
        return l;
    }

    void echo(int $indent) {
        String indent = "";
        while (indent.length() < $indent) {
            indent += "  ";
        }
        println(indent + "---- POLYLINE ----");
        for (int i = 0; i < nsegments; i++) {
            segments[i].echo($indent + 1);
        }
    }

    void render(PApplet surface) {
        for (int i = 0; i < nsegments; i++) {
            segments[i].render(surface);
        }
    }
}
