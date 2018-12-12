package rcp_impl.rcp_model;

import mvc.model.CityVisualizationModel;
import mvc.model.dimension.time.IntervalType;
import mvc.model.dimension.time.TimeRange;
import mvc.model.members.Facility;
import mvc.model.members.Sensor;
import mvc.model.widgets.IWidgetModel;
import mvc.model.widgets.TimeSeriesWidgetModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A singleton model, contains:
 *  - a time range utility model
 *
 * Input:
 *  - a list of facilities with facts about them (type or geo-positiion)
 *  - a one facility to many measurements mapping data
 *  - time range
 *  - a recorded data (timestamp vs measured values)
 *  - a RCP prediction data (timestamp vs prediction values)
 */
public enum RCPVisualizer_Model implements CityVisualizationModel {
  INSTANCE;

  // Data Model
  private List<Facility> listOfFacilities = new ArrayList<>();
  private Map<Integer, List<Sensor>> mapOfFacilityToSensors;

  // Application Model
  private TimeRange timeModule = new TimeRange();
  private List<IWidgetModel> widgetModelList = new ArrayList<>();

  @Override
  public void setAppTimeRange(long startUnix, IntervalType spanType) {
    timeModule = new TimeRange(startUnix, spanType);
  }

  @Override
  public void incrementTime() {
    timeModule.increment15Min();
  }

  @Override
  public long getCurrentTime() {
    return timeModule.getCurUnix();
  }

  @Override
  public long getStartTime() {
    return timeModule.getStartUnix();
  }

  @Override
  public long getEndTime() {
    return timeModule.getEndUnix();
  }

  @Override
  public void addFacility(Facility f) {
    listOfFacilities.add(f);
  }

  @Override
  public void removeFacility(int facilityID) {
    for (Facility f : listOfFacilities) {
      if (f.getID() == facilityID) {
        listOfFacilities.remove(f);
        return;
      }
    }
  }

  @Override
  public List<Facility> getFacilityList() {
    List<Facility> copy = new ArrayList<>();
    for (Facility f : listOfFacilities) {
      copy.add(f.clone());
    }
    return copy;
  }

  @Override
  public List<IWidgetModel> getWidgetList() {
    List<IWidgetModel> copy = new ArrayList<>();
    for (IWidgetModel f : widgetModelList) {
      copy.add(f.clone());
    }
    return copy;
  }

  @Override
  public void addWidget(IWidgetModel newWidget) {
    widgetModelList.add(newWidget);
  }

}
