package rcp_impl.rcp_model;

import mvc.model.members.Facility;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NortheasternBuilding implements Facility {

  private final int BUILDING_ID;
  private final String SHORTNAME;
  private final String NAME;
  private final String USE;
  private final int FLOORS;
  private final float AREA;

  private final int COLOR;
  private final Point2D.Float CENTROID;
  private final int[] SENSORS;
  private final List<Point2D.Float> VERTICES;

  public NortheasternBuilding(int bID,
                              String shortName,
                              String name,
                              String usage,
                              int floors,
                              float area,
                              int color,
                              Point2D.Float center,
                              int[] sensorIDs,
                              List<Point2D.Float> vertices) {
    BUILDING_ID = bID;
    SHORTNAME = Objects.requireNonNull(shortName);
    NAME = Objects.requireNonNull(name);
    USE = Objects.requireNonNull(usage);
    FLOORS = floors;
    AREA = area;

    COLOR = color;
    CENTROID = Objects.requireNonNull(center);
    SENSORS = Objects.requireNonNull(sensorIDs);
    VERTICES = Objects.requireNonNull(vertices);
  }

  @Override
  public int getID() {
    return BUILDING_ID;
  }

  @Override
  public String getUse() {
    return USE;
  }

  @Override
  public int getFloors() {
    return FLOORS;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getColor() {
    return COLOR;
  }

  @Override
  public Point2D.Float getCenter() {
    return CENTROID;
  }

  @Override
  public List<Point2D.Float> getVertices() {
    return Collections.unmodifiableList(VERTICES);
  }

  @Override
  public float getArea() {
    return AREA;
  }

  public int[] getSensors() {
    return SENSORS;
  }


  public String getShortName() {
    return SHORTNAME;
  }

  @Override
  public Facility clone() {
    int[] clonedSensorIds = new int[SENSORS.length];
    for (int i = 0; i < SENSORS.length; ++i) {
      clonedSensorIds[i] = SENSORS[i];
    }

    List<Point2D.Float> clonedVertices = new ArrayList<>(VERTICES.size());
    for (int i = 0; i < VERTICES.size(); ++i) {
      clonedVertices.add((Point2D.Float) VERTICES.get(i).clone());
    }

    return new NortheasternBuilding(
            BUILDING_ID,
            SHORTNAME,
            NAME,
            USE,
            FLOORS,
            AREA,
            COLOR,
            (Point2D.Float) CENTROID.clone(),
            clonedSensorIds,
            clonedVertices);
  }
}
