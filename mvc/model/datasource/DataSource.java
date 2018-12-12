package mvc.model.datasource;

import java.util.List;
import java.util.Optional;

import mvc.model.dimension.time.IntervalType;
import mvc.model.dimension.time.TimeUtil;
import processing.data.Table;
import processing.data.TableRow;

/**
 * Represents a panel (longitudinal) data source (numerical or non-numerical) with a single variable.
 * Currently holds all available data inside a processing.data.Table.
 * Indexing will store the recently using data range in an array for fast access.
 *
 * The constructor will calculate
 * @param <T> the variable data type
 */
public abstract class DataSource<T> implements IData<DataSource<T>> {

  protected boolean showWarnings = false;

  protected Table dataTable;

  protected long indexedUnixStart, indexedUnixEnd;
  protected long entireMinUnix, entireMaxUnix;

  protected boolean isIncreasing, isHugging;

  protected Optional<T> maxValue = Optional.empty();
  protected Optional<T> minValue = Optional.empty();
  protected Optional<Integer> idxAtLocalMax = Optional.empty();
  protected Optional<Integer> idxAtLocalMin = Optional.empty();
  protected Optional<T> localMax = Optional.empty();
  protected Optional<T> localMin = Optional.empty();

  protected Optional<Long> maxUnix = Optional.empty();
  protected Optional<Long> minUnix = Optional.empty();
  protected Optional<Integer> idxAtLocalMaxUnix = Optional.empty();
  protected Optional<Integer> idxAtLocalMinUnix = Optional.empty();
  protected Optional<Long> localMaxUnix = Optional.empty();
  protected Optional<Long> localMinUnix = Optional.empty();

  protected Optional<Integer> startRowIndex = Optional.empty();
  protected Optional<Integer> endRowIndex = Optional.empty();

  protected IntervalType sampleInterval;

  protected String valueColName;
  protected String unixColumnName;

  protected List<Optional<Tuple<Long, T>>> indexedValues; // use this for unhugged ranges only!

  /**
   * This is for wholesome data with entire ranges covered ONLY, with no missing data!!
   * Will break if bad data is given.
   * @param sourcedata
   * @param mainUnixStart
   * @param mainUnixEnd
   */
  public DataSource(Table sourcedata,
                    long mainUnixStart, long mainUnixEnd,
                    IntervalType sampleInterval,
                    String idxColName, String valueColName) {

    this.sampleInterval = sampleInterval;

    this.unixColumnName = idxColName;
    this.valueColName = valueColName;

    this.dataTable = sourcedata;

    this.indexedUnixStart = mainUnixStart;
    this.indexedUnixEnd = mainUnixEnd;

    this.entireMinUnix = findUnixStart();
    this.entireMaxUnix = findUnixEnd();

    this.isIncreasing = this.isChronoOrder();
  }

  public String getValueColName() {
    return valueColName;
  }

  public String getUnixColumnName() {
    return unixColumnName;
  }

  public IntervalType getSampleInterval() {
    return sampleInterval;
  }

  // Finds the lowest unix time of the source (which is sorted in order; lowest of first or last row assumed to be the start)
  private long findUnixStart() {
    TableRow firstRow = dataTable.getRow(0);
    TableRow lastRow = dataTable.getRow(dataTable.getRowCount() - 1);
    // Processing bug: if getLong is "", it returns a 0...
    if (firstRow.getLong(unixColumnName) <= lastRow.getLong(unixColumnName)) {
      return firstRow.getLong(unixColumnName);
    } else {
      return lastRow.getLong(unixColumnName);
    }
  }

  // Finds the highest unix time of the source (which is sorted in order; highest of first or last row assumed to be the end);
  private long findUnixEnd() {
    TableRow firstRow = dataTable.getRow(0);
    TableRow lastRow = dataTable.getRow(dataTable.getRowCount() - 1);

    if (firstRow.getLong(unixColumnName) <= lastRow.getLong(unixColumnName)) {
      return lastRow.getLong(unixColumnName);
    } else {
      return firstRow.getLong(unixColumnName);
    }
  }

  // Determines whether the data is in time order; from 0 to last row increasing (assumes data is sorted)
  boolean isChronoOrder() {
    TableRow firstRow = dataTable.getRow(0);
    TableRow lastRow = dataTable.getRow(dataTable.getRowCount() - 1);

    return firstRow.getLong(unixColumnName) <= lastRow.getLong(unixColumnName);
  }

  /**
   * Determines whether the start unix of this data is ON THE SAME SAMPLE INTERVAL OR LESS THAN the start unix of the parent
   * Note: this allows a view to peek at the interval at indexedUnixStart,
   *       even though indexedUnixStart is in the middle of the same interval as entireMinUnix
   */
  protected boolean isStartUnixValid(long toCompare) {
    return TimeUtil.compareUnixValuesBasedOnIntervalSegment(entireMinUnix, toCompare, sampleInterval) <= 0;
  }

  /**
   * Determines whether the end unix of this data is ON THE SAME SAMPLE INTERVAL OR MORE THAN the end unix of the parent
   * Note: this allows a view to peek at the interval at indexedUnixEnd,
   *       even though indexedUnixEnd is in the middle of the same interval as entireMaxUnix
   */
  protected boolean isEndUnixValid(long toCompare) {
    return TimeUtil.compareUnixValuesBasedOnIntervalSegment(entireMaxUnix, toCompare, sampleInterval) >= 0;
  }

  /**
   * Find Max and min values for the entire data set.
   */
  protected abstract void updateOverallMaxMin();

  // Local value/unix max min accessors
  public Optional<T> getLocalMax() {
    return localMax;
  }
  public Optional<T> getLocalMin() {
    return localMin;
  }
  public Optional<T> getMaxVal() {
    return maxValue;
  }
  public Optional<T> getMinVal() {
    return minValue;
  }
  public Optional<Integer> getIndexOfLocalMax() {
    return idxAtLocalMax;
  }
  public Optional<Integer> getIndexOfLocalMin() {
    return idxAtLocalMax;
  }

  public Optional<Long> getLocalMaxUnix() {
    return localMaxUnix;
  }
  public Optional<Long> getLocalMinUnix() {
    return localMinUnix;
  }
  public Optional<Long> getMaxUnix() {
    return maxUnix;
  }
  public Optional<Long> getMinUnix() {
    return minUnix;
  }
  public Optional<Integer> getIndexOfLocalMaxUnix() {
    return idxAtLocalMaxUnix;
  }
  public Optional<Integer> getIndexOfLocalMinUnix() {
    return idxAtLocalMinUnix;
  }

  public abstract void updateLocalMax();

  public abstract void updateLocalMin();

  /**
   * Calculates the max unix in the indexed array, then updates the variables holding the value and its index position
   */
  public void updateLocalMaxUnix() {
    Optional<Long> max = Optional.empty();
    Optional<Integer> index = Optional.empty();

    for(int i = 0; i < getIndexedArray().size(); ++i) {
      Optional<Tuple<Long, T>> curVal = getIndexedArray().get(i);
      if (curVal.isPresent() && max.isPresent()) {
        if (curVal.get().x.compareTo(max.get()) > 0){
          max = Optional.of(curVal.get().x);
          index = Optional.of(i);
        }
      } else if (curVal.isPresent()) {
        max = Optional.of(curVal.get().x);
        index = Optional.of(i);
      }
    }
    this.idxAtLocalMaxUnix = index;
    this.localMaxUnix = max;
  }

  /**
   * Calculates the min unix in the indexed array, then updates the variables holding the value and its index position
   */
  public void updateLocalMinUnix() {
    Optional<Long> min = Optional.empty();
    Optional<Integer> index = Optional.empty();

    for (int i = 0; i < getIndexedArray().size(); ++i) {
      Optional<Tuple<Long, T>> curVal = getIndexedArray().get(i);
      if (curVal.isPresent() && min.isPresent()) {
        if (curVal.get().x.compareTo(min.get()) < 0){
          min = Optional.of(curVal.get().x);
          index = Optional.of(i);
        }
      } else if (curVal.isPresent()) {
        min = Optional.of(curVal.get().x);
        index = Optional.of(i);
      }
    }

    this.idxAtLocalMinUnix = index;
    this.localMinUnix = min;
  }

  /**
   * Retrieves the index for the given unix based on the current sampleInterval,
   * starting the search at index 0.
   * What counts as a match:
   *   - requestedTime is at the same sample interval as another unix time
   *
   * Precondition: Data is ordered by increasing time
   * @param requestedTime in unix seconds
   * @return Optional.empty() if no match, Optional.of(index) if match
   */
  public abstract Optional<Integer> requestIndexAtUnix(long requestedTime);

  /**
   * Retrieves the index for the given unix based on the current sampleInterval,
   * starting the search at the index startIndex.
   * What counts as a match:
   *   - requestedTime is at the same sample interval as another unix time
   *
   * Precondition: Data is ordered by increasing time
   * @param requestedTime in unix seconds
   * @param startIndex what index to start searching from
   * @return Optional.empty() if no match, Optional.of(index) if match
   */
  public abstract Optional<Integer> requestIndexAtUnix(long requestedTime, int startIndex);

  public abstract Optional<T> requestValueAtUnix(long requestedTime);

  /**
   * Requests a value based on the column name @ the given index.
   * @param index any positive number
   */
  public abstract Optional<T> requestValue_TableIndex(int index);

  /**
   * Requests a unix based on the column nama e@ the given index
   */
  public Optional<Long> requestUnix_TableIndex(int index) {
    if (index < 0 || index >= dataTable.getRowCount()) {
      return Optional.empty();
    }
    Long result = dataTable.getRow(index).getLong(unixColumnName);
    if (result == null) {
      return Optional.empty();
    }
    return Optional.of(result);
  }

  // Updates:
  // 	 If the source is hugging the new timerange, make efficient adjustments, and index
  //     else: index normally
  public abstract void updateIndexRange(long updatedStart, long updatedEnd, IntervalType newSegmentLength);

  public abstract List<Optional<Tuple<Long, T>>> getIndexedArray();

  /**
   * Visits the relevant DataSource<T>, calls the type-appropriate method to get T
   * with the {@code valColName}, then outputs Optional<T>
   * @param row
   * @return
   */
  protected abstract Optional<T> getValueFromRow(TableRow row);

  public Optional<Tuple<Long, T>> requestValue_BoundedIndex(int index) {
    if (index < 0 || index >= getIndexedArray().size()) {
      warning("Accessing OOB index in requestValue_BoundedIndex, given " + index + "!");
      return Optional.empty();
    }
    return getIndexedArray().get(index);
  }

  protected void setSampleInterval(IntervalType newSegmentLength) {
    this.sampleInterval = newSegmentLength;
  }

  protected void println(String s) {
    System.out.println(s);
  }

  protected void print(String s) {
    System.out.print(s);
  }

  public void warning(String warning) {
    if (showWarnings) {
      System.out.println(warning);
    }
  }

  @Override
  public void accept(IDataVisitor dv) {
    dv.visit(this);
  }

  @Override
  public DataSource<T> unbox() {
    return this;
  }
}