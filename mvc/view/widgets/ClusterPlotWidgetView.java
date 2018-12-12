package mvc.view.widgets;

import mvc.model.datasource.*;
import mvc.model.datasource.Container;
import mvc.model.widgets.IWidgetModel;
import mvc.view.viewmodel.CityVisualizationViewModel;
import processing.core.PApplet;
import processing.core.PGraphics;
import visitors.widgetview.IWidgetViewVisitor;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static processing.core.PApplet.sort;
import static processing.core.PConstants.CENTER;

/**
 * Created by redbeans on 1/6/17.
 */
public class ClusterPlotWidgetView extends AbstractBoxWidgetView {

    private PGraphics buffer;
    private int cx, cy;
    private Circle[] circles;

    private Map<Integer, TemporalNumberDS<Float>> proj, base;


    public ClusterPlotWidgetView(Map<Integer, TemporalNumberDS<Float>> proj, Map<Integer, TemporalNumberDS<Float>> base) {
        super(900, 900, "0");
        this.proj = proj;
        this.base = base;
    }

    @Override
    public IWidgetModel.WidgetType getWidgetType() {
        return IWidgetModel.WidgetType.Cluster;
    }

    @Override
    public void accept(IWidgetViewVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void render(PApplet parentView, CityVisualizationViewModel viewDataModel) {
        buffer = parentView.createGraphics(super.w, super.h);
        buffer.beginDraw();

        IDataVisitor<mvc.model.datasource.Container<List<String>>> dataVisitor = new DataVisitor<>();
        viewDataModel.get("facilityNames").accept(dataVisitor);
        List<String> names = dataVisitor.getResponse().unbox();

        IDataVisitor<Container<List<Point2D.Float>>> dataVisitor2 = new DataVisitor<>();
        viewDataModel.get("facilityCenters").accept(dataVisitor2);
        dataVisitor2.getResponse().unbox();
        List<Point2D.Float> centers = dataVisitor2.getResponse().unbox();


        int numCircles = proj.keySet().size() - 1; // -1 if no power plant

        cx = w / 2;
        cy = h / 2;

        List<Tuple<Integer, Integer>> idAndSize = new ArrayList<>(numCircles);
        for (Integer i : proj.keySet()) {
            if (i == 26) {
                continue;
            }
            Float value = proj.get(i).getIndexedArray().get(monthNumer).get().y / 5f; // 10 if pp, otherwise 5
            idAndSize.add(new Tuple<>(i, value.intValue()));
        }
        Collections.sort(idAndSize, (a, b) -> (a.y).compareTo(b.y));

        int indexToSkip = 0;

        circles = new Circle[numCircles];
        for (int i = 0; i < numCircles; i++) {
            Tuple<Integer, Integer> toTurn = idAndSize.get(numCircles-1-i);
            Integer facilityID = toTurn.x;
            String label = names.get(facilityID);

            if (label.equals("Ell")) {
                indexToSkip = i;
            }

            circles[i] = new Circle(facilityID, toTurn.y, label,
                    (int) centers.get(facilityID).x,
                    (int) centers.get(facilityID).y);
        }

        circles[indexToSkip].x = cx;
        circles[indexToSkip].y = cy;
        circles[indexToSkip].computed = true;

        for (int i=0; i < numCircles; i++) {
            if (i == indexToSkip) {
                continue;
            }
            circles[i].computePosition(circles);
        }

        buffer.background(255);

        for (int i=0; i < numCircles; i++) {
            circles[i].display(buffer);
        }

        buffer.endDraw();
    }

    @Override
    public void removeHoveredStates() {
        // DO NOTHING
    }

    @Override
    public void resetToDefault() {
        // DO NOTHING
    }

    @Override
    public void draw(PApplet s) {
        pushPosition(s);
        s.image(buffer, 0, 0);
        popPosition(s);
    }

    private int monthNumer = 11;

    class Circle {
        private int x, y;
        private int radius;
        private boolean computed = false;
        private String name;
        private int facilityID;

        Circle(int facilityID, int radius, String label) {
            this.radius = radius;
            this.name = label;
            this.facilityID = facilityID;
        }

        Circle(int facilityID, int radius, String label, int x, int y) {
            this.radius = radius;
            this.name = label;
            this.facilityID = facilityID;
            this.x = x;
            this.y = y;
        }

        void computePosition(Circle[] c) {
            int i, j;
            boolean collision;
            Point[] openPoints = new Point[0];
            int ang;
            Point pnt;

            if (computed) { return; }

            for (i=0; i<c.length; i++) {
                if (c[i].computed) {
                    ang = 0;
                    for (ang=0; ang<360; ang+=1) {
                        collision = false;
                        pnt = new Point();
                        pnt.x = c[i].x + (int) PApplet.cos(ang * PApplet.PI / 180) * (radius+c[i].radius+1);
                        pnt.y = c[i].y + (int) PApplet.sin(ang* PApplet.PI / 180) * (radius+c[i].radius+1);

                        for (j=0; j<c.length; j++) {
                            if (c[j].computed && !collision) {
                                if (PApplet.dist(pnt.x, pnt.y, c[j].x, c[j].y) < radius + c[j].radius) {
                                    collision = true;
                                }
                            }
                        }

                        if (!collision) {
                            openPoints =  (Point[]) PApplet.expand(openPoints, openPoints.length+1);
                            openPoints[openPoints.length-1] = pnt;
                        }
                    }
                }
            }

            float min_dist = -1;
            int best_point = 0;
            for (i=0; i<openPoints.length; i++)
            {
                if (min_dist == -1 || PApplet.dist(cx, cy, openPoints[i].x, openPoints[i].y) < min_dist)
                {
                    best_point = i;
                    min_dist = PApplet.dist(cx, cy, openPoints[i].x, openPoints[i].y);
                }
            }
            if (openPoints.length == 0) {
                System.out.println("No points.");
            } else
            {
                System.out.println(openPoints.length + " points");
                x = openPoints[best_point].x;
                y = openPoints[best_point].y;
            }

            computed = true;
        }

        void display(PGraphics buffer) {

            float baseValue = base.get(facilityID).getIndexedArray().get(monthNumer).get().y;
            float thisValue = proj.get(facilityID).getIndexedArray().get(monthNumer).get().y;
            float percentDelta = (thisValue - baseValue) / baseValue;
            if (percentDelta > 0) {
                // Blue pos
                buffer.fill(66, 134, 244);
                buffer.stroke(66, 134, 244);
            } else {
                // Red neg
                buffer.fill(239, 47, 47);
                buffer.stroke(239, 47, 47);
            }

            buffer.textAlign(CENTER, CENTER);
            buffer.ellipseMode(CENTER);
//            buffer.strokeWeight(2);
            buffer.noStroke();
//            buffer.ellipse(x, y, radius*2, radius*2);

            buffer.noStroke();

            // A slice of pie starts at Radians(-90 degrees)
            float sliceOriginAngle = PApplet.radians(-90);

            // The slice amount + direction
            float sliceAngle = PApplet.radians(360 * percentDelta);

            // What is rendered:
            float startSlice = 0;
            float endSlice = 0;
            float startNonSlice = 0;
            float endNonSlice = 0;

            if (sliceAngle < 0) {
                startSlice = sliceOriginAngle + sliceAngle; // sliceAngle negative here
                endSlice = sliceOriginAngle;
                startNonSlice = sliceOriginAngle;
                endNonSlice = sliceOriginAngle + PApplet.radians(360) + sliceAngle; // sliceAngle negative here
            } else {
                startSlice = sliceOriginAngle;
                endSlice = sliceOriginAngle + sliceAngle;
                startNonSlice = sliceOriginAngle + sliceAngle;
                endNonSlice = sliceOriginAngle + PApplet.radians(360);
            }

            // Render
            buffer.arc(x, y, radius * 2, radius * 2, startSlice, endSlice);         // Slice
            buffer.fill(230);
            buffer.arc(x, y, radius * 2, radius * 2, startNonSlice, endNonSlice);   // Non-slice

            buffer.pushStyle();
            buffer.fill(35);
            buffer.text(name, x, y);
            buffer.popStyle();
        }
    }
}
