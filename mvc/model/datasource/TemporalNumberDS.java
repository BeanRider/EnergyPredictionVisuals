package mvc.model.datasource;

import mvc.model.dimension.time.IntervalType;
import mvc.model.dimension.time.TimeUtil;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import processing.data.Table;
import processing.data.TableRow;

/**
 * Represents a numerically valued data source, indexed by time (unix seconds)
 * Only numerical data can have: min, max, range and so on.
 * @param <N> a numerical, comparable type
 */
public abstract class TemporalNumberDS<N extends Number & Comparable<? super N>> extends DataSource<N> {


  public TemporalNumberDS(Table sourcedata, long indexUnixStart,
                          long indexUnixEnd, IntervalType sampleInterval,
                          String unixColumnName,
                          String desiredColumn) {
    super(sourcedata, indexUnixStart, indexUnixEnd, sampleInterval,
            unixColumnName, desiredColumn);
    updateOverallMaxMin();
    updateIndexRange(indexUnixStart, indexUnixEnd, sampleInterval);
    printStatus(0, indexUnixStart, indexUnixEnd);
  }

  /**
   * Numerical data only:
   * @return
   */
  public abstract Optional<N> getRange();

  /**
   * Numerical data only
   * @param target
   * @return
   */
  public abstract Optional<N> diffFromMin(N target);

  /**
   * Numerical data only
   * @param other
   * @return
   */
  public abstract TemporalNumberDS<N> delta(TemporalNumberDS<N> other);

  public abstract TemporalNumberDS<Float> percentDelta(TemporalNumberDS<Float> y);

  public abstract TemporalNumberDS<Float> percentDeltaRetain(TemporalNumberDS<Float> y);

  public abstract TemporalNumberDS<N> combine(NumberOperation<N, N> lam);

  public abstract TemporalNumberDS<N> condenseByTime(IntervalType intervalType);

  @Override
  protected void updateOverallMaxMin() {
    Optional<N> curMax = Optional.empty();
    Optional<N> curMin = Optional.empty();
    for (TableRow row : dataTable.rows()) {
      Optional<N> i = getValueFromRow(row);

      if (!i.isPresent()) {
        // DO NOTHING, no value at row
      } else {
        // If curMax is not present OR (isPresent AND bigger) -> replace with maybeMax
        if (!curMax.isPresent() || (curMax.isPresent() && i.get().compareTo(curMax.get()) > 0)) {
          curMax = i;
        }

        if (!curMin.isPresent() || (curMin.isPresent() && i.get().compareTo(curMin.get()) < 0)) {
          curMin = i;
        }
      }
    }
    maxValue = curMax;
    minValue = curMin;
  }

  @Override
  public void updateLocalMax() {
    Optional<N> max = Optional.empty();
    Optional<Integer> index = Optional.empty();

    for(int i = 0; i < getIndexedArray().size(); ++i) {
      Optional<Tuple<Long, N>> curVal = getIndexedArray().get(i);
      if (curVal.isPresent() && max.isPresent()) {
        if (curVal.get().y.compareTo(max.get()) > 0){
          max = Optional.of(curVal.get().y);
          index = Optional.of(i);
        }
      } else if (curVal.isPresent()) {
        max = Optional.of(curVal.get().y);
        index = Optional.of(i);
      }
    }
    this.idxAtLocalMax = index;
    this.localMax = max;
  }

  @Override
  public void updateLocalMin() {
    Optional<N> min = Optional.empty();
    Optional<Integer> index = Optional.empty();

    for (int i = 0; i < getIndexedArray().size(); ++i) {
      Optional<Tuple<Long, N>> curVal = getIndexedArray().get(i);
      if (curVal.isPresent() && min.isPresent()) {
        if (curVal.get().y.compareTo(min.get()) < 0){
          min = Optional.of(curVal.get().y);
          index = Optional.of(i);
        }
      } else if (curVal.isPresent()) {
        min = Optional.of(curVal.get().y);
        index = Optional.of(i);
      }
    }

    this.idxAtLocalMin = index;
    this.localMin = min;
  }

  /**
   * Updates:
   *  If the source is hugging the new time range, make efficient adjustments, and index/
   *  else: index normally
   *
   * @param updatedStart
   * @param updatedEnd
   * @param newSegmentLength
   */
  @Override
  public void updateIndexRange(long updatedStart, long updatedEnd, IntervalType newSegmentLength) {

//    println("Updating " + this.valueColName + "...");

    long startTime = System.nanoTime();
    setSampleInterval(newSegmentLength);

    this.indexedUnixStart = updatedStart;
    this.indexedUnixEnd = updatedEnd;

    // Case 1: The min && max of the data source is "hugging" the new range.
    // This supports peeking.
    // Look at documentation for isStartUnixValid or isEndUnixValid for details.
    if (isStartUnixValid(updatedStart) && isEndUnixValid(updatedEnd)) {
      isHugging = true;

      // 1. Find start and end indices
      Optional<Integer> startIndexO = requestIndexAtUnix(updatedStart);
      Optional<Integer> endIndexO = requestIndexAtUnix(updatedEnd);
      Integer startIndex;
      Integer endIndex;
      if (startIndexO.isPresent() && endIndexO.isPresent()) {
        startIndex = startIndexO.get();
        endIndex = endIndexO.get();
      } else {
        throw new RuntimeException("start/end index not found!");
      }

      // 2. Index
      int size = endIndex - startIndex;
//      println(""+size);
      indexedValues = new ArrayList<>(size);
      for (int i = startIndex; i < endIndex; ++i) {
        Optional<Long> u = requestUnix_TableIndex(i);
        Optional<N> v = requestValue_TableIndex(i);
        if (!u.isPresent() || !v.isPresent()) {
          indexedValues.add(Optional.empty());
        } else {
          indexedValues.add(Optional.of(new Tuple(u.get(), v.get())));
        }

      }
//      System.out.println(indexedValues);

      this.startRowIndex = Optional.of(startIndex);
      this.endRowIndex = Optional.of(endIndex);
    }
    // Case 2: Not hugging
    else {
      isHugging = false;
      DateTime startDate = new DateTime(updatedStart * 1000L);
      DateTime endDate = new DateTime(updatedEnd * 1000L);
      int numPositions = TimeUtil.computePeriodsBetween(startDate, endDate, sampleInterval);

//      println("Source timerange (total)  : " + this.entireMinUnix + " - " + this.entireMaxUnix);
//      println(""+numPositions);

      indexedValues = new ArrayList<>(numPositions);
      for (int i = 0; i < numPositions; ++i) {
        indexedValues.add(Optional.empty());
      }

      Optional<Integer> foundIndex = Optional.empty();
      for (int i = 0; i < numPositions; ++i) {
        if (foundIndex.isPresent()) {
          foundIndex = requestIndexAtUnix(TimeUtil.computeDatePlusInterval(startDate, sampleInterval, i).getMillis() / 1000L, foundIndex.get());
        } else {
          foundIndex = requestIndexAtUnix(TimeUtil.computeDatePlusInterval(startDate, sampleInterval, i).getMillis() / 1000L, 0);
        }
        if (foundIndex.isPresent()) {
          Optional<Long> u = requestUnix_TableIndex(i);
          Optional<N> v  = requestValue_TableIndex(foundIndex.get());
          if (u.isPresent() && v.isPresent()) {
            indexedValues.set(i, Optional.of(new Tuple(u.get(), v.get())));
          }

        }
      }
    }

    updateLocalMin();
    updateLocalMax();
    updateLocalMinUnix();
    updateLocalMaxUnix();
  }

  public abstract void printStatus(long startTime, long updatedStart, long updatedEnd);

  @Override
  public Optional<Integer> requestIndexAtUnix(long reqUnix) {
    return requestIndexAtUnix(reqUnix, 0);
  }

  @Override
  public Optional<Integer> requestIndexAtUnix(long reqUnix, int startIndex) {
    // Early exit checks
    if (!isStartUnixValid(reqUnix) ||  !isEndUnixValid(reqUnix)) {
      return Optional.empty();
    }

    // Search for such a time, comparingUnixValuesBasedOnCurrentInterval (this is assuming increasing time!)
    for (int i = startIndex; i < dataTable.getRowCount(); ++i) {
      TableRow row = dataTable.getRow(i);
      long unixAtI = row.getLong(unixColumnName);
//      System.out.println("i=" + i);
//      System.out.println("tableValue=" + unixAtI);
//      System.out.println("reqUnix=" + reqUnix);

      int result = TimeUtil.compareUnixValuesBasedOnIntervalSegment(unixAtI, reqUnix, sampleInterval);
      if (result < 0) {
        // Keep going, but add to i to jump ahead (assuming in "regular time period intervals"
        // (does not necessarily mean equal intervals, for example, a month in december is different from one in november),
        // and assuming increasing time).
        // Question: how many intervals of sampleInterval are between the requested time, and the time right now?
        int indicesToJump = TimeUtil.computePeriodsBetween(
                new DateTime(unixAtI * 1000L),
                new DateTime(reqUnix * 1000L),
                sampleInterval);
        indicesToJump--; // this is because for loop increments 1 more at the end of this loop
        if (indicesToJump < 0) {
          throw new RuntimeException("Index jumping failed!");
        }
        i += indicesToJump;
      } else if (result == 0) {
        // Found
        return Optional.of(i);
      } else {
        // Not found
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  /**
   * Visits the relevant implementation of the DS for type N, then returns the value with
   * {@code valColName} and given row index.
   * @param index any positive number
   * @return
   */
  @Override
  public abstract Optional<N> requestValue_TableIndex(int index);

  @Override
  public Optional<N> requestValueAtUnix(long requestedTime) {
    Optional<Integer> maybeIndex = requestIndexAtUnix(requestedTime, 0);
    if (maybeIndex.isPresent()) {
      return requestValue_TableIndex(maybeIndex.get());
    }
    return Optional.empty();
  }

  @Override
  public List<Optional<Tuple<Long, N>>> getIndexedArray() {
    return indexedValues;
  }

  /**
   * Returns the Moment object representing the local max
   * @return the Optional<Moment<N>> of the local max within the indexed list.
   */
  public Optional<Moment<N>> getLocalPeakValue() {
    if (getIndexOfLocalMax().isPresent() && getLocalMax().isPresent()) {
      return Optional.of(new Moment<>(
              requestUnix_TableIndex(startRowIndex.get() + getIndexOfLocalMax().get()).get(),
              getLocalMax().get(),
              valueColName));
    }
    return Optional.empty();
  }

  /**
   * Returns the Moment object representing the local min
   * @return the Optional<Moment<N>> of the local min within the indexed list.
   */
  public Optional<Moment<N>> getLocalBaseValue() {
    if (getIndexOfLocalMin().isPresent() && getLocalMin().isPresent()) {
      return Optional.of(new Moment<>(
              requestUnix_TableIndex(startRowIndex.get() + getIndexOfLocalMin().get()).get(),
              getLocalMin().get(),
              valueColName));
    }
    return Optional.empty();
  }
}
