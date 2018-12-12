package main;

import mvc.view.CityVisualizationView;
import processing.core.PApplet;
import rcp_impl.rcp_view.RCPVisualizer_DynamicView;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

/**
 * Execution entry point
 */
public class RCPVisualizerMain {

  public static void main(String args[]) {
    TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"));
    DateTimeZone.setDefault(DateTimeZone.forID("America/New_York"));

//    PApplet.main(new String[] {"--present", "utility.voro.VoronoiMain"});
//    System.exit(0);

    CityVisualizationView v = new RCPVisualizer_DynamicView();
//    CityVisualizationController c = new RCPVisualizer_Controller();

//    v.init(c);
    v.run();
//    c.init(v, m);
  }
}
