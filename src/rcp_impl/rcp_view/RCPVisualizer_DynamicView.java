package rcp_impl.rcp_view;

import mvc.controller.CityVisualizationController;
import mvc.model.datasource.*;
import mvc.model.datasource.Container;
import mvc.view.CityVisualizationView;
import mvc.view.UIAction.Action;
import mvc.view.UIAction.ActionSuite;
import mvc.view.viewmodel.CityVisualizationViewModel;
import mvc.view.viewmodel.RCPVisualizer_ViewModel;
import utility.ViewModelUtil;
import visitors.viewmodel.RCPViewModelDataVisitor;
import visitors.widgetview.WidgetViewDrawVisitor;
import visitors.widgetview.WidgetViewRenderVisitor;
import visitors.widgetview.WidgetViewUpdateVisitor;
import mvc.view.widgets.IWidgetView;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import utility.GraphicsAlgorithms;
import org.joda.time.DateTime;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import processing.data.TableRow;
import rcp_impl.rcp_controller.RCPVisualizer_Controller;
import utility.voro.*;
import utility.voro.Polygon;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static utility.voro.Global.global;

/**
 * View of the program;
 * Input:
 *  - a list of facilities with facts about them (type or geo-positiion)
 *  - a one facility to many measurements mapping data
 *  - time range
 *  - a recorded data (timestamp vs measured values)
 *  - a RCP prediction data (timestamp vs prediction values)
 *
 *
 * Ideas:
 *  - a widget-based view
 *  - dimensions: time, geo
 *
 */
public class RCPVisualizer_DynamicView extends PApplet implements CityVisualizationView {

  // MVC Architecture
  private CityVisualizationController controller;
  private CityVisualizationViewModel viewModel = new RCPVisualizer_ViewModel();

  private Dimension nativeWH;
  public static PFont fLato_14, fArial_14;

  private DateTime startTime = new DateTime();
  private DateTime endTime = new DateTime();
  private DateTime currentTime = new DateTime();

  // Cached data
  private TableRow currentProjections = null;

  // View Objects

  /** Widgets */
  private List<IWidgetView> widgetViews = new ArrayList<>();

  private List<utility.voro.Point> bounds = new ArrayList<>();
  private Voronoi diagram = null;
  private Polygon stage;

  @Override
  public void setup() {
    nativeWH = new Dimension(displayWidth, displayHeight);
    fLato_14 = loadFont("Lato-Regular-14-all.vlw");
    fArial_14 = loadFont("ArialUnicodeMS-14-all.vlw");
    controller = new RCPVisualizer_Controller();
    controller.initView(this);

    global = new Global();
    global.init(width, height);
  }

  public void init(CityVisualizationController c) {
    controller = c;
  }

  public void run() {
    PApplet.main(new String[] {"--present", "rcp_impl.rcp_view.RCPVisualizer_DynamicView"});
  }

  @Override
  public Dimension getDimension() {
    return nativeWH;
  }

  @Override
  public void draw() {
    background(40);

    // default font: Lato 14 with color(100) fill
    textFont(fLato_14, 14);
    fill(100);

    // Render layers
//    drawMapWithFacilities();
//    drawVoronoi();
//    drawBoundary();

    drawWidgetViews();
//    drawAppTimeVisuals();
//    controller.tick();
  }

  // ======================================
  // Client-side Requests to the controller
  // ======================================

  // TODO Interactions to View

  // =======================================
  // Entry points to mutate the view's state
  // =======================================

  @Override
  public void updateAppTimeVisuals(long startTime, long currentTime, long endTime) {
    this.startTime = new DateTime(startTime * 1000L);
    this.currentTime = new DateTime(currentTime * 1000L);
    this.endTime = new DateTime(endTime * 1000L);

    for (IWidgetView w : widgetViews) {
      // Uses the visitor pattern by sending a package.
      w.accept(new WidgetViewUpdateVisitor(this.currentTime));
    }
  }

  @Override
  public void addWidgetVisuals(List<IWidgetView> iWidgetViews) {
    this.widgetViews.addAll(iWidgetViews);
    int i = 0;
    for (IWidgetView wv : this.widgetViews) {
      int w = 1780;
      int h = 400;
//      wv.setDimension(w, h);
//      wv.setCornerXY((displayWidth - w) / 2, 10 + i * (h+10));
//      wv.setCornerXY(
//              (displayWidth - wv.getDimension().width) / 2,
//              (displayHeight - wv.getDimension().height) / 2);
      ActionSuite widgetActionSuite = new ActionSuite();
      widgetActionSuite.setScrolledAction(new Action() {
        @Override
        public void act(CityVisualizationController controller, MouseEvent e) {
          wv.addCornerXY(0, e.getCount());
        }
      });
      widgetActionSuite.setHoveredAction(new Action() {
        @Override
        public void act(CityVisualizationController controller, MouseEvent e) {
          wv.enableHoverTooltip(true);
        }
      });
      wv.bindAction(widgetActionSuite);
      wv.accept(new WidgetViewRenderVisitor(this, viewModel));
      i++;
    }
  }

  @Override
  public void updateViewModel(String name, IData data) {
    viewModel.put(name, data);
  }

  // ==================================================================
  // All render methods: render based on the current state of this view
  // ==================================================================

  /**
   * Render the boundary for the voronoi diagram
   */
  private void drawBoundary() {
    pushMatrix();
    pushStyle();
    strokeWeight(2);
    stroke(200);
    fill(255, 100);
    translate(width / 2, height / 2);
    if (bounds.size() > 3) {
      utility.voro.Point[] poly = new utility.voro.Point[bounds.size()];
      for (int i = 0; i < bounds.size(); ++i) {
        poly[i] = bounds.get(i);
      }
      Polygon toRender = new Polygon(poly);
      toRender.render2D(this);
    }
    popStyle();
    popMatrix();
  }

  /**
   * Render the current time
   */
  private void drawAppTimeVisuals() {
    int day = currentTime.getDayOfYear();
//    println(currentTime.getMillis());
    text("Day of year = " + day, 30, 30);
    text("Month = " + currentTime.getMonthOfYear(), 30, 55);
  }

  /**
   * Render the generated voronoi diagram
   */
  private void drawVoronoi() {
    if (diagram != null) {
      pushMatrix();
      translate(width / 2, height / 2, 0);

      stroke(0, 100);
      strokeWeight(1);
      noFill();
      diagram.render(this);
      popMatrix();
    }
  }

  /**
   * Render geographical representation of facilities.<br />
   * Currently featured: building shape, building centroid
   */
  private void drawMapWithFacilities() {
    Map<Integer, List<PShape>> facilityOutlines = new ViewModelUtil<Map<Integer, List<PShape>>>().getContainer("facilityOutlines", viewModel);
    List<Point2D.Float> facilityCenters = new ViewModelUtil<List<Point2D.Float>>().getContainer("facilityCenters", viewModel);

    pushMatrix();
    translate(displayWidth / 2, displayHeight / 2);

    pushStyle();

    fill(79);
    noStroke();

    int fIdx = 0;
    for (List<PShape> shapeList : facilityOutlines.values()) {

      // Centroid
      float centroidX = facilityCenters.get(fIdx).x;
      float centroidY = facilityCenters.get(fIdx).y;
      ellipse(centroidX, centroidY, 10, 10);

      // Shapes related to this building
      for (PShape shape : shapeList) {
        shape(shape);
      }
      ++fIdx;
    }
    popStyle();
    popMatrix();
  }

  private void drawWidgetViews() {
    for (IWidgetView wv : widgetViews) {
      wv.accept(new WidgetViewDrawVisitor(this));
    }
  }

  // ================================================================
  // All controller methods: pass controller events to the controller
  // ================================================================

  @Override
  public void mouseWheel(MouseEvent e) {
    for (IWidgetView v : widgetViews) {
      v.mouseScrollAction(e, controller);
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    List<Point2D.Float> facilityCenters = new ViewModelUtil<List<Point2D.Float>>().getContainer("facilityCenters", viewModel);

    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
      for (IWidgetView v : widgetViews) {
        v.addCornerXY(15, 0);
      }
    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
      for (IWidgetView v : widgetViews) {
        v.addCornerXY(-15, 0);
      }
    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
      if (diagram == null) {
        //initialize voronoi diagram
        utility.voro.Point[] ps = new utility.voro.Point[facilityCenters.size()];
        for (int i = 0; i < facilityCenters.size(); ++i) {
          ps[i] = new utility.voro.Point(facilityCenters.get(i).x, facilityCenters.get(i).y, 0);
        }

        utility.voro.Point[] poly = new utility.voro.Point[bounds.size()];
        System.out.println("Bounds:");
        for (int i = 0; i < bounds.size(); ++i) {
          poly[i] = bounds.get(i);
          System.out.println(poly[i].x + ", " + poly[i].y);
        }
        this.stage = new Polygon(poly);
        diagram = new Voronoi(ps, this.stage);
        return;
      }

      for (int i = 0; i < 2; ++i) {
        diagram = GraphicsAlgorithms.singleLloyds(diagram.getAllCellCentroids(), stage);
        System.out.println(i);
      }
      for (utility.voro.Point c : diagram.getAllCellCentroids()) {
        System.out.println(c.x + ", " + c.y);
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    for (IWidgetView wv : widgetViews) {
      if (wv.isMouseOver(e.getX(), e.getY())) {
        wv.mouseHoverAction(controller, e);
        break;
      } else {
        wv.removeHoveredStates();
      }
    }
  }

  @Override
  public void mousePressed() {
    bounds.add(new utility.voro.Point(mouseX - width / 2, mouseY - height / 2,0));
  }

  @Override
  public void settings() {
    size(displayWidth, displayHeight, P3D);
  }
}
