package rcp_impl.rcp_controller;

import mvc.controller.CityVisualizationController;
import mvc.model.CityVisualizationModel;
import mvc.model.datasource.Container;
import mvc.model.datasource.TemporalNumberDS;
import mvc.model.datasource.NumberDSFloat;
import mvc.model.dimension.ColorUtil;
import mvc.model.dimension.time.IntervalType;
import mvc.model.members.Facility;
import mvc.model.widgets.*;
import visitors.widgetmodel.WidgetModelToViewVisitor;
import mvc.view.widgets.IWidgetView;
import processing.core.PApplet;
import processing.core.PShape;
import processing.data.Table;
import rcp_impl.rcp_model.RCPVisualizer_Model;
import rcp_impl.rcp_model.NortheasternBuilding;
import mvc.view.CityVisualizationView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import utility.GeoUtil;
import utility.GraphicsAlgorithms;
import utility.StringParseUtil;

import java.awt.geom.Point2D;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Contains code that processes data, user input, and display output
 */
public class RCPVisualizer_Controller implements CityVisualizationController {

  private CityVisualizationView view;
  private CityVisualizationModel model;

  @Override
  public void init(CityVisualizationView v, CityVisualizationModel m) {
    model = m;
    view = v;
  }

  @Override
  public void initView(CityVisualizationView v) {
    model = RCPVisualizer_Model.INSTANCE;
    view = v;

    // Controller parsing logic, then pass results into model
    // Temp: directly specifying file name.

    // TODO Import building csv file using CommonsCSV
    Iterable<CSVRecord> recordsIterable = loadCSV("./src/data/buildingProperties.csv");
    List<CSVRecord> savedRecords = new ArrayList<>();

    Point2D.Float originLatLong = null;
    for (CSVRecord record : recordsIterable) {
      savedRecords.add(record);
      if (record.get("bID").equals("12")) {
        originLatLong = StringParseUtil.parseCentroidString(record.get("Centroid"));
      }
    }

    for (CSVRecord record : savedRecords) {

      System.out.println("Record " + record.get("bID"));
      // Get Building Color
      //    categoryColors,  // Color mapping
//      int bColor = getBuildingColor(false);

      // Parse a String (set of sets) -> a list of String/Points (list of points)
      List<String> verticesAsStringSets = StringParseUtil.getListFromSet(record.get("Outline"));
      List<Point2D.Float> vertices = new ArrayList<>();
      for (String s : verticesAsStringSets) {
        vertices.add(GeoUtil.convertGeoToScreen(
                StringParseUtil.parseCentroidString(s),
                originLatLong,
                v.getDimension().width,
                v.getDimension().height)); // Converts to a PVector using the current screen res.
      }

      model.addFacility(
              new NortheasternBuilding(
                      Integer.parseInt(record.get("bID")), // ID
                      record.get("shortName"), // Short name
                      record.get("Name"), // Name
                      record.get("Primary Use"), // USE
                      Integer.parseInt(record.get("Floors")), // Floors
                      Float.parseFloat(record.get("Footprint")), // area
                      ColorUtil.color(230, 230, 230),
                      // Parse string -> centroid point
//                      buildingToSensorsMap.get(bIndex).toArray()),

                      GeoUtil.convertGeoToScreen(
                              StringParseUtil.parseCentroidString(record.get("Centroid")),
                              originLatLong,
                              view.getDimension().width,
                              view.getDimension().height),
                      new int[0],
                      vertices));
    }

    // Todo, replace hardcoded time range with smart time detection through data analysis
    model.setAppTimeRange(1357016400, IntervalType.YEAR);

    Table projectionsTable = ((PApplet)v).loadTable("projection.csv", "header");

    // Todo, replace hardcoded column selection to user-picked columns
    String currentBuilding = "";
    TemporalNumberDS<Float> currentBase = null;
    List<TemporalNumberDS<Float>> percentDeltas = new ArrayList<>();
    List<TemporalNumberDS<Float>> bases = new ArrayList<>();
    List<TemporalNumberDS<Float>> projections = new ArrayList<>();
    List<TemporalNumberDS<Float>> deltas = new ArrayList<>();

    for (int i = 2; i < projectionsTable.getColumnTitles().length; ++i) {
      String columnName = projectionsTable.getColumnTitles()[i];
      String currentBID = columnName.split("_")
              [0];
      if (currentBuilding.equals(currentBID)) {
        // Same building as before
        TemporalNumberDS<Float> projection = new NumberDSFloat(
                projectionsTable,
                model.getStartTime(), model.getEndTime(),
                IntervalType.DAY,
                "timestamp", columnName);
        assert currentBase != null;
        // FIXME condenseByTime condenses by the given interval type
        TemporalNumberDS<Float> baseByMonth = currentBase.condenseByTime(IntervalType.MONTH);
        TemporalNumberDS<Float> projByMonth = projection.condenseByTime(IntervalType.MONTH);
        TemporalNumberDS<Float> percentDeltaByMonth = baseByMonth.percentDeltaRetain(projByMonth);
        TemporalNumberDS<Float> deltaByMonth = baseByMonth.delta(projByMonth);

        String[] test = columnName.split("_");
        if (!test[test.length - 1].equals("85")) {
          bases.add(baseByMonth);
          projections.add(projByMonth);
          percentDeltas.add(percentDeltaByMonth);
          deltas.add(deltaByMonth);
        }
      } else {
        // Not the same building
        currentBase = new NumberDSFloat(
                projectionsTable,
                model.getStartTime(), model.getEndTime(),
                IntervalType.DAY,
                "timestamp", columnName);
      }
      currentBuilding = currentBID;
    }

    // =======================
    // Configure Widgets Here:
    // =======================
//    model.addWidget(new TreemapWidgetModelBuilder()
//            .setDataSource(percentDeltas)
//            .setRange(new TimeRange(1357016400, IntervalType.YEAR))
//            .build());

    Map<Integer, TemporalNumberDS<Float>> percentDeltaMap = new HashMap<>();
    for (TemporalNumberDS<Float> projectionDelta : percentDeltas) {
//      model.addWidget(new TimeSeriesWidgetModelBuilder()
//              .setDataSource(projectionDelta)
//              .setRange(new TimeRange(1357016400, IntervalType.YEAR))
//              .build());

      int fIdx = Integer.parseInt(projectionDelta.getValueColName().split("_")[0]);
      percentDeltaMap.put(fIdx, projectionDelta);
    }

    Map<Integer, TemporalNumberDS<Float>> baseMap = new HashMap<>();
    for (TemporalNumberDS<Float> b : bases) {
//      model.addWidget(new TimeSeriesWidgetModelBuilder()
//              .setDataSource(projectionDelta)
//              .setRange(new TimeRange(1357016400, IntervalType.YEAR))
//              .build());

      int fIdx = Integer.parseInt(b.getValueColName().split("_")[0]);
      baseMap.put(fIdx, b);
    }

    Map<Integer, TemporalNumberDS<Float>> projectionMap = new HashMap<>();
    for (TemporalNumberDS<Float> b : projections) {
      int fIdx = Integer.parseInt(b.getValueColName().split("_")[0]);
      projectionMap.put(fIdx, b);
    }


    model.addWidget(new ViolinPlotWidgetModel.Builder().setDataSource(projectionMap, baseMap).build());

    // size = projected kWh/sqft, pie = % delta kWh/sqft
    model.addWidget(new ClusterPlotWidget.Builder().setDataSource(projectionMap, baseMap).build());

//    model.addWidget(new GeoSnapShotWidgetModelBuilder()
//            .setDataSource(percentDeltaMap)
//            .setOriginLatLong(originLatLong)
//            .setRange(new TimeRange(1357016400, IntervalType.YEAR))
//            .build());


    // Initialize shared view objects used by multiple widgets
    buildFacilityMapVisuals(model.getFacilityList());


    // Build View Objects
    view.addWidgetVisuals(buildWidgetViews(model.getWidgetList()));

    // 1. Fetch from model, 2. Generate graphics
    view.updateAppTimeVisuals(model.getStartTime(), model.getCurrentTime(), model.getEndTime());
  }

  private void initializeWidgetBehavior() {

  }

  private static IntervalType DEFAULT = IntervalType.YEAR;

  // ======================================================
  // Private View Construction
  // 1. Controller ASKS Model for information
  // 2. Controller builds view objects with the information
  // 3. Controller passes view objects to main view
  // ======================================================

  // Cached objects (shared by multiple views)

  // Given widget models, build and returns widget views
  private List<IWidgetView> buildWidgetViews(List<IWidgetModel> widgetModels) {
    List<IWidgetView> widgetViews = new ArrayList<>();
    for (IWidgetModel w : widgetModels) {
      w.accept(new WidgetModelToViewVisitor(widgetViews));
    }
    return widgetViews;
  }

  // Given facility models, build and returns outlines and centers
  private void buildFacilityMapVisuals(List<Facility> facilities) {
    Map<Integer, List<PShape>> facilityOutlines = new HashMap<>(facilities.size());
    List<Point2D.Float> facilityCenters = new ArrayList<>(facilities.size());
    List<String> facilityNames = new ArrayList<>(facilities.size());
    List<Float> facilityAreas = new ArrayList<>(facilities.size());
    view.updateViewModel("facilityOutlines", new Container<>(facilityOutlines));
    view.updateViewModel("facilityCenters", new Container<>(facilityCenters));
    view.updateViewModel("facilityNames", new Container<>(facilityNames));
    view.updateViewModel("facilityAreas", new Container<>(facilityAreas));

    // 2. Initialize with new facility map visuals
    for (Facility f : facilities) {
      // outlines
      if (facilityOutlines.containsKey(f.getID())) {
        facilityOutlines.get(f.getID()).add(GraphicsAlgorithms.buildPolygon(f.getVertices(), f.getColor(), (PApplet) view));
      } else {
        List<PShape> newValues = new ArrayList<>();
        newValues.add(GraphicsAlgorithms.buildPolygon(f.getVertices(), f.getColor(), (PApplet) view));
        facilityOutlines.put(f.getID(), newValues);
      }
      // center points
      facilityCenters.add((Point2D.Float) f.getCenter().clone());
      // names
      facilityNames.add(f.getName());
      // Area
      facilityAreas.add(f.getFloors() * f.getArea());
    }
  }

  private void FGU_facilities() {
    // 1. Fetch from model
    List<Facility> facilities = model.getFacilityList();
    // 2. Generate graphics
    this.buildFacilityMapVisuals(facilities);
    // 3. Pass to view.
  }

  // =============================
  // View Manipulation
  // Updates the state of the view
  // =============================

  @Override
  public void tick() {
    for (int i = 0; i < 5; ++i) {
      model.incrementTime();
    }
    view.updateAppTimeVisuals(model.getStartTime(), model.getCurrentTime(), model.getEndTime());
  }

  private Iterable<CSVRecord> loadCSV(String filename) {
    Iterable<CSVRecord> records = null;
    try {
      Reader in = new FileReader(filename);
      records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
    return records;
  }

  /**
   * Get building color from the mapping
   * @param isNewName version of naming (new / old)
   * @return new java.awt.Color
   */
  private int getBuildingColor(boolean isNewName) {

//    String buildingColorAsString = null;
//    for (int i = 0; i < buildingTypeRenameTable.getRowCount(); ++i) {
//      TableRow row = buildingTypeRenameTable.getRow(i);
//      if (isNewName && row.getString("newName").equals(USE)) {
//        buildingColorAsString = row.getString("newRGB");  // switch to "rgb" if old colors
//      } else if (!isNewName && row.getString("oldName").equals(USE)) {
//        buildingColorAsString = row.getString("newRGB");  // switch to "rgb" if old colors
//      }
//    }
//
//    try {
//      Objects.requireNonNull(buildingColorAsString);
//    } catch (NullPointerException e) {
//      e.printStackTrace();
//    }
//    String[] buildingRGBAsString = buildingColorAsString.split(",");
//    int[] buildingRGB = new int[3];
//    for (int i = 0; i < 3; ++i) {
//      buildingRGB[i] = Integer.parseInt(buildingRGBAsString[i].trim());
//    }
//    return color(buildingRGB[0], buildingRGB[1], buildingRGB[2]);

    return ColorUtil.color(250, 250, 250);
  }




}