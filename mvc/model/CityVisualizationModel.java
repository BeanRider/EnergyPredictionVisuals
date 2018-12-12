package mvc.model;

import mvc.model.dimension.time.IntervalType;
import mvc.model.members.Facility;
import mvc.model.widgets.IWidgetModel;
import mvc.model.widgets.TimeSeriesWidgetModel;

import java.util.List;

/**
 * Input:
 *  - a list of facilities with facts about them (type or geo-positiion)
 *  - a one facility to many measurements mapping data
 *  - time range
 *  - a recorded data (timestamp vs measured values)
 *  - a RCP prediction data (timestamp vs prediction values)
 */
public interface CityVisualizationModel {

  /**
   * Adds the given facility to the model
   * @param f
   */
  void addFacility(Facility f);

  /**
   * Removes the facility with given facilityID, if exists, from this model.
   * @param facilityID
   */
  void removeFacility(int facilityID);

  /**
   * @return a deep copy of the facility list in this model.
   */
  List<Facility> getFacilityList();

  void setAppTimeRange(long startUnix, IntervalType spanType);

  long getCurrentTime();

  long getStartTime();

  long getEndTime();

  void incrementTime();

  List<IWidgetModel> getWidgetList();

  void addWidget(IWidgetModel newWidget);
}
