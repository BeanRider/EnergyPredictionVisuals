package mvc.model.datasource;

import java.util.Optional;
import mvc.model.dimension.time.IntervalType;
import org.joda.time.DateTime;
import processing.data.Table;
import processing.data.TableRow;

public class NumberDSFloat extends TemporalNumberDS<Float> {

  public NumberDSFloat(Table sourcedata,
                       long mainUnixStart, long mainUnixEnd,
                       IntervalType sampleInterval,
                       String unixColumnName, String desiredColumn) {
    super(sourcedata,
            mainUnixStart, mainUnixEnd,
            sampleInterval,
            unixColumnName, desiredColumn);
  }

  @Override
  public Optional<Float> getRange() {
    if (getMaxVal().isPresent() && getMinVal().isPresent()) {
      return Optional.of(getMaxVal().get() - getMinVal().get());
    }
    return Optional.empty();
  }

  @Override
  public Optional<Float> diffFromMin(Float target) {
    if (getMinVal().isPresent()) {
      return Optional.of(target - getMinVal().get());
    }
    return Optional.empty();
  }

  @Override
  public TemporalNumberDS<Float> delta(TemporalNumberDS<Float> y) {
    String deltaColumnName = super.getValueColName() + "Delta" + y.getValueColName();
    Table newTableWithDelta = new Table();
    newTableWithDelta.addColumn(unixColumnName);
    long[] thisUnixColumn = super.dataTable.getLongColumn(unixColumnName);
    for (int i = 0; i < thisUnixColumn.length; ++i) {
      long u = thisUnixColumn[i];
      newTableWithDelta.setLong(i, unixColumnName, u);
    }

    newTableWithDelta.addColumn(deltaColumnName);
    newTableWithDelta.setRowCount(super.dataTable.getRowCount());

    for (int i = 0; i < dataTable.getRowCount(); ++i) {
      Optional<Float> otherValue = y.requestValue_TableIndex(i);
      Optional<Float> thisValue = this.requestValue_TableIndex(i);
      if (otherValue.isPresent() && thisValue.isPresent())
        newTableWithDelta.setFloat(i, deltaColumnName, otherValue.get() - thisValue.get());
      else
        throw new RuntimeException("Delta for " + deltaColumnName + " has failed.");
    }
    return new NumberDSFloat(newTableWithDelta,
                              this.indexedUnixStart, this.indexedUnixEnd,
                              this.sampleInterval,
                              unixColumnName, deltaColumnName);
  }

  @Override
  public TemporalNumberDS<Float> percentDelta(TemporalNumberDS<Float> y) {
    String deltaColumnName = super.getValueColName() + "percentDelta" + y.getValueColName();
    Table newTableWithDelta = new Table();
    newTableWithDelta.addColumn(unixColumnName);
    long[] thisUnixColumn = super.dataTable.getLongColumn(unixColumnName);
    for (int i = 0; i < thisUnixColumn.length; ++i) {
      long u = thisUnixColumn[i];
      newTableWithDelta.setLong(i, unixColumnName, u);
    }

    newTableWithDelta.addColumn(deltaColumnName);
    newTableWithDelta.setRowCount(super.dataTable.getRowCount());

    for (int i = 0; i < dataTable.getRowCount(); ++i) {
      Optional<Float> otherValue = y.requestValue_TableIndex(i);
      Optional<Float> thisValue = this.requestValue_TableIndex(i);
      if (otherValue.isPresent() && thisValue.isPresent())
        newTableWithDelta.setFloat(i, deltaColumnName, (otherValue.get() - thisValue.get()) / thisValue.get());
      else
        throw new RuntimeException("percentDelta for " + deltaColumnName + " has failed.");
    }
    return new NumberDSFloat(newTableWithDelta,
            this.indexedUnixStart, this.indexedUnixEnd,
            this.sampleInterval,
            unixColumnName, deltaColumnName);
  }

  public TemporalNumberDS<Float> percentDeltaRetain(TemporalNumberDS<Float> y) {
    String deltaColumnName = super.getValueColName() + "percentDelta" + y.getValueColName();
    Table newTableWithDelta = new Table();
    newTableWithDelta.addColumn(unixColumnName);
    long[] thisUnixColumn = super.dataTable.getLongColumn(unixColumnName);
    for (int i = 0; i < thisUnixColumn.length; ++i) {
      long u = thisUnixColumn[i];
      newTableWithDelta.setLong(i, unixColumnName, u);
    }

    newTableWithDelta.addColumn(deltaColumnName);
    newTableWithDelta.addColumn(super.getValueColName());
    newTableWithDelta.addColumn(y.getValueColName());
    newTableWithDelta.setRowCount(super.dataTable.getRowCount());

    for (int i = 0; i < super.dataTable.getRowCount(); ++i) {
      Optional<Float> otherValue = y.requestValue_TableIndex(i);
      Optional<Float> thisValue = this.requestValue_TableIndex(i);
      if (otherValue.isPresent() && thisValue.isPresent()) {
        newTableWithDelta.setFloat(i, deltaColumnName, (otherValue.get() - thisValue.get()) / thisValue.get());
        newTableWithDelta.setFloat(i, super.getValueColName(), thisValue.get());
        newTableWithDelta.setFloat(i, y.getValueColName(), otherValue.get());
      } else {
        throw new RuntimeException("percentDelta for " + deltaColumnName + " has failed.");
      }
    }
    return new NumberDSFloat(newTableWithDelta,
            this.indexedUnixStart, this.indexedUnixEnd,
            this.sampleInterval,
            unixColumnName, deltaColumnName);
  }

  @Override
  public TemporalNumberDS<Float> combine(NumberOperation<Float, Float> lam) {
    // TODO
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public TemporalNumberDS<Float> condenseByTime(IntervalType newTimeSegment) {
    // for each newTimeSegment in data, sum up all value within that segment
    String condensedColumnName = super.getValueColName() + "_by" + newTimeSegment.name();
    Table newTableWithDelta = new Table();
    newTableWithDelta.addColumn(unixColumnName);
    newTableWithDelta.addColumn(condensedColumnName);
    long[] thisUnixColumn = super.dataTable.getLongColumn(unixColumnName);
    float[] thisValueColumn = super.dataTable.getFloatColumn(valueColName);
    long previousAddedUnix = 0;
    int previousAddedIndex = -1;
    for (int i = 0; i < thisUnixColumn.length; ++i) {
      long u = thisUnixColumn[i];
      // TODO change so it depends on given interval type
      long utruncated;
      switch (newTimeSegment) {
        case MONTH:
          utruncated = new DateTime(u * 1000L).withDayOfMonth(1).withMillisOfDay(0).getMillis() / 1000L;
          break;
        case WEEK:
          utruncated = new DateTime(u * 1000L).withDayOfWeek(1).withMillisOfDay(0).getMillis() / 1000L;
          break;
        default:
          throw new RuntimeException("Bad enum");
      }

      if (previousAddedUnix != utruncated) {
        // on a new time segment, increase new index number
        previousAddedIndex++;
        newTableWithDelta.setLong(previousAddedIndex, unixColumnName, utruncated);
        previousAddedUnix = utruncated;
        newTableWithDelta.setFloat(previousAddedIndex, condensedColumnName, thisValueColumn[i]);
      } else {
        // on the same time segment, add to the old one
        float oldValue = newTableWithDelta.getFloat(previousAddedIndex, condensedColumnName);
        newTableWithDelta.setFloat(previousAddedIndex, condensedColumnName, oldValue + thisValueColumn[i]);
      }
    }
    return new NumberDSFloat(newTableWithDelta,
            super.indexedUnixStart, super.indexedUnixEnd,
            newTimeSegment,
            unixColumnName, condensedColumnName);
  }



  @Override
  public Optional<Float> getValueFromRow(TableRow row) {
    Float value = row.getFloat(valueColName);
    if (value == null || value.isNaN()) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  @Override
  public Optional<Float> requestValue_TableIndex(int index) {
    if (index < 0 || index >= dataTable.getRowCount()) {
      warning("WARNING: requestValue_TableIndex for " + index + " is OOB!");
      return Optional.empty();
    }
    Float result = dataTable.getRow(index).getFloat(valueColName);
    if (result == null || Float.isNaN(result)) {
      return Optional.empty();
    }
    return Optional.of(result);
  }

  @Override
  public void printStatus(long startTime, long updatedStart, long updatedEnd) {
    println("Indexing took: " + (System.nanoTime() - startTime) + " ns!");
    println("=== DataSource (" + valueColName + ") =====================");
    println("Indexed time range: " + indexedUnixStart + " to " + indexedUnixEnd);
    println("Entire data time range : " + entireMinUnix + " to " + entireMaxUnix);
    println("Visualization Sample Interval: " + sampleInterval.name());
    println("Min = " + this.minValue);
    println("Max = " + this.maxValue);
    println("");
    println("");
  }
}
