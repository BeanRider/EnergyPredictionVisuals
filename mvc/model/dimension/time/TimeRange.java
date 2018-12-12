package mvc.model.dimension.time;

import org.joda.time.*;

/**
 * Represents a time span, including its bounds, interval, and current value
 */
public class TimeRange {

  public static final int START = -1;
  public static final int CURRENT = 0;
  public static final int END = 1;

  private DateTime start;
  // INVARIANT: START <= CURRENT <= (END - subInterval)
  private DateTime current;
  private DateTime end;

  private IntervalType spanType;

  // For caching
  private int segmentCount = 0;

  /**
   * Constructs by: Setting a starting time, length, and interval
   * @param startUnix   - initial begin time
   * @param spanType    - entire segment length
   */
  public TimeRange(long startUnix, IntervalType spanType) {
    DateTimeZone.setDefault(DateTimeZone.forID("America/New_York"));

    updateTime(startUnix, spanType);

    System.out.println(start.getMillis());
    System.out.println(end.getMillis());
  }

  // Clone constructor
  private TimeRange(DateTime start, DateTime current, DateTime end,
                    IntervalType spanType, int segmentCount) {
    this.start = start;
    this.current = current;
    this.end = end;
    this.spanType = spanType;
    this.segmentCount = segmentCount;
  }

  /**
   * Constructs by: Setting starting time as beginning of system year, in one year interval, using system time zone
   */
  public TimeRange() {
    DateTime beginningOfSystemYear = new DateTime(new DateTime().getYear(), 1, 1, 0, 0, 0, 000, DateTimeZone.getDefault());

    updateTime(beginningOfSystemYear.getMillis() / 1000, IntervalType.YEAR);
  }

  /**
   * MUTATION: Updates the timeline in terms of starting unix and timeline length
   * @param newUnixStartTime in seconds
   * @param newLength
   */
  public void updateTime(long newUnixStartTime, IntervalType newLength) {
    spanType = newLength;

    start = new DateTime(newUnixStartTime * 1000L);
    current = new DateTime(start);
    end = TimeUtil.computeDatePlusInterval(start, spanType, 1);

    // Cache
    segmentCount = TimeUtil.compute15Minutes(start, end);
  }

  /**
   * MUTATION: Updates the timeline in terms of starting unix, keeping the previous timeline length
   * @param newUnixStartTime in seconds
   */
  private void updateTime(long newUnixStartTime) {
    start = new DateTime(newUnixStartTime * 1000L);
    current = new DateTime(start);
    end = TimeUtil.computeDatePlusInterval(start, spanType, 1);

    // Cache
    segmentCount = TimeUtil.compute15Minutes(start, end);
  }

  /**
   * MUTATION: Updates the current index of the timeline
   * @param index
   */
  public void scrubTo(int index) {
    DateTime newCurrentTime = start.plusMinutes(index * 15);
//    System.out.println("Attempting to scrub to index = " + index + ";");
//    System.out.println("New Current Time = " + newCurrentTime.getMillis());
//    System.out.println("Start Time = " + start.getMillis());
    if (checkDateRange(newCurrentTime)) {
      throw new RuntimeException(index + " isn't something you can scrub to!");
    } else {
      current = newCurrentTime;
    }
  }

  public void setCurrentTime(DateTime c) {
    if (checkDateRange(c)) {
      throw new RuntimeException(c + " isn't something you can scrub to!");
    } else {
      this.current = c;
    }
  }

  /**
   * MUTATION: Move forward in time by one increment (depending on the IntervalType subSegmentLength);
   * if incremented on the ending index of this current timeline, then jump to next section.
   */
  public void increment15Min() {
    DateTime incrementedDateTime = current.plusMinutes(15);
    if (checkDateRange(incrementedDateTime)) {
      jumpToNextSection();
    } else {
      current = current.plusMinutes(15);
    }
  }

  /**
   * Move backward in time by one increment (depending on the IntervalType subSegmentLength);
   * if incremented on the starting index of this current timeline, jump to prev section.
   */
  public void decrement() {
    DateTime decrementedDateTime = current.minusMinutes(15);
    if (checkDateRange(decrementedDateTime)) {
      jumpToPrevSection();
    } else {
      current = current.minusMinutes(15);
    }
  }

  /**
   * @return the start unix value of this timeline
   */
  public long getStartUnix() {
    return start.getMillis() / 1000L;
  }

  /**
   * @return the current unix value of this timeline
   */
  public long getCurUnix() {
    return current.getMillis() / 1000L;
  }

  /**
   * @return the end unix value of this timeline
   */
  public long getEndUnix() {
    return end.getMillis() / 1000L;
  }

  /**
   * @return the current index this timeline
   */
  public int getCurIdx() {
    // TODO possibly called intensively, might need caching current index
    return (int) ((current.getMillis() - start.getMillis()) / (1000 * 60 * 15));
  }

  /**
   * @return the last index this timeline, which is the last portion of time this timeline can scrub to.
   */
  public int getEndIdx() {
    return segmentCount - 1;
  }

  /**
   * @return The DateTime in which will take this interval to the next interval
   */
  public DateTime getJumpDateTime() {
    return end.minusMinutes(15);
  }

  /**
   * If the given DateTime in millis if under start or over or equal to end millis, return true.
   * @param d
   * @return
   */
  private boolean checkDateRange(DateTime d) {
    return d.getMillis() < start.getMillis() || end.getMillis() <= d.getMillis();
  }

  /**
   * Returns DateTime for the given option
   * @param option CURRENT, START, END
   * @return DateTime depending on option
   */
  public DateTime toDateTimeFor(int option) {
    switch(option) {
      case START:
        return new DateTime(start);
      case CURRENT:
        return new DateTime(current);
      case END:
        return new DateTime(end);
    }
    throw new RuntimeException("That is not an option for date!");
  }

  /**
   * Returns the Date object that the given index is referring to in this timeline
   * @param index
   * @return Date the given index is referring to in this timeline
   * @throws RuntimeException, if the given index is not within the timeline right now.
   */
  public DateTime getDateForIndex(int index) {
    DateTime testDate = start.plusMinutes(index * 15);
    if (checkDateRange(testDate)) {
      throw new RuntimeException(index + " is not within the current timerange!");
    }
    // System.out.println("ms = " + (unixStart + index * 15 * 60) * 1000L);
    return testDate;
  }

  /**
   * Jumps to the next section of time
   */
  public void jumpToNextSection() {
    updateTime(getEndUnix());
  }

  /**
   * Jumps to the beginning of the previous section (defined by spanType) of time
   */
  public void jumpToPrevSection() {
    DateTime d = toDateTimeFor(CURRENT);

    long unixNextStart;

    switch (spanType) {
      case YEAR:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0)
                .withDayOfMonth(1)
                .withMonthOfYear(1);
        unixNextStart = d.minusYears(1).getMillis() / 1000;
        break;
      case MONTH:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0)
                .withDayOfMonth(1);
        unixNextStart = d.minusMonths(1).getMillis() / 1000;
        break;
      case WEEK:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0);
        unixNextStart = d.minusWeeks(1).getMillis() / 1000;
        break;
      case DAY:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0);
        unixNextStart = d.minusDays(1).getMillis() / 1000;
        break;
      case HOUR:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0);
        unixNextStart = d.minusHours(1).getMillis() / 1000;
        break;
      default:
        throw new RuntimeException(spanType.name() + " cannot be recognized!");
    }
    updateTime(unixNextStart);
  }

  /**
   * @return the timeline's interval type
   */
  public IntervalType getIntervalType() {
    return spanType;
  }

  @Override
  public TimeRange clone() {
    return new TimeRange(new DateTime(start), new DateTime(current), new DateTime(end), spanType, segmentCount);
  }
}
