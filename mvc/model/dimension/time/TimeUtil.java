package mvc.model.dimension.time;

import org.joda.time.*;

import java.text.SimpleDateFormat;

/**
 * Time-related Utility Methods
 */
public class TimeUtil {
  public static int compareUnixValuesBasedOnIntervalSegment(long unix1, long unix2, IntervalType segmentLength) {
    switch (segmentLength) {
      case QUARTER:
        return TimeUtil.compareQuarterly(unix1, unix2);
      case HOUR:
        return TimeUtil.compareHourly(unix1, unix2);
      case DAY:
        return TimeUtil.compareDaily(unix1, unix2);
      case WEEK:
        return TimeUtil.compareWeekly(unix1, unix2);
      case MONTH:
        return TimeUtil.compareMonthly(unix1, unix2);
      case YEAR:
        return TimeUtil.compareYearly(unix1, unix2);
      default:
        throw new RuntimeException("Bad interval enum given!!");
    }
  }

  /**
   * @return how many days are in this interval
   */
  public static int compute15Minutes(DateTime start, DateTime end) {
    return Hours.hoursBetween(start, end).getHours() * 4;
  }

  /**
   * Pre-condition: given start must be rounded in terms of 15 minutes
   * @param start rounded in 15 minutes
   * @param length length of time after start
   * @return number of 15 minutes between start and start + length
   */
  public static int computeTimeSegments(DateTime start, IntervalType length, IntervalType frequency) {
    return computePeriodsBetween(start, computeDatePlusInterval(start, length, 1), frequency);
  }

  public static int computePeriodsBetween(DateTime start, DateTime end, IntervalType frequency) {
    switch (frequency) {
      case QUARTER:
        return Minutes.minutesBetween(start, end).getMinutes() / 15;
      case HOUR:
        return Hours.hoursBetween(start, end).getHours();
      case DAY:
        return Days.daysBetween(start, end).getDays();
      case WEEK:
        return Weeks.weeksBetween(start, end).getWeeks();
      case MONTH:
        return Months.monthsBetween(start, end).getMonths();
      case YEAR:
        return Years.yearsBetween(start, end).getYears();
      default:
        throw new EnumConstantNotPresentException(IntervalType.class, "Bad enum");
    }
  }

  public static DateTime computeDatePlusInterval(DateTime start, IntervalType length, int times) {
    switch (length) {
      case YEAR:
        return start.plusYears(times);
      case MONTH:
        return start.plusMonths(times);
      case WEEK:
        return start.plusWeeks(times);
      case DAY:
        return start.plusDays(times);
      case HOUR:
        return start.plusHours(times);
      case QUARTER:
        return start.plusMinutes(times * 15);
      default:
        throw new RuntimeException("Unsupported IntervalType: " + length.name());
    }
  }

  /**
   * Convert the given eastern YMD into unix time
   * @param year given eastern year
   * @param month given eastern month
   * @param day given eastern day
   * @return converted unix timestamp
   */
  public static long getUnixInEastern(int year, int month, int day, int hour) {
    DateTime dateTime = new DateTime(year, month, day, hour, 0, 0);
    return dateTime.getMillis() / 1000L;
  }

  /**
   * @param year
   * @return how many days in given year
   */
  public static int daysInYear(int year) {
    DateTime dateTime = new DateTime(year, 1, 14, 0, 0, 0, 000);
    return dateTime.dayOfYear().getMaximumValue();
  }

  /**
   * @param year
   * @param month
   * @return how many days in given year and month
   */
  public static int daysInMonth(int year, int month) {
    DateTime dateTime = new DateTime(year, month, 14, 0, 0, 0, 000);
    return dateTime.dayOfMonth().getMaximumValue();
  }

  /**
   * @param unix
   * @return the day of week of the given unix
   */
  public static int getDayOfWeek(long unix) {
    return new DateTime(unix * 1000L).getDayOfWeek();
  }

  /**
   * @param unix
   * @return the character of the day of week of the given unix
   */
  public static char getDayOfWeekLetter(long unix) {
    return formatDayAsChar(getDayOfWeek(unix));
  }

  /**
   * Returns the HH:mm digital time of the given DateTime
   * @return the HH:mm digital time string of the given DateTime
   */
  public static String getDigitalTime(DateTime t) {
    return String.format("%02d", t.getHourOfDay()) + ":" +
            String.format("%02d", t.getMinuteOfHour());
  }

  /**
   * @param i
   * @return given the number representation of a day of week, return the appropriate DoW character
   */
  public static char formatDayAsChar(int i) {
    DateTime date = new DateTime().withDayOfWeek(i);
    return date.dayOfWeek().getAsText().charAt(0);
  }

  /**
   * Compares two Unix timestamps if they are on the same hour.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareHourly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int day1 = d1.getDayOfMonth();
    int day2 = d2.getDayOfMonth();
//    	System.out.println(day1 + " vs. " + day2);

    int hour1 = d1.getHourOfDay();
    int hour2 = d2.getHourOfDay();

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (day1 < day2) {
          return -1;
        } else if (day1 > day2) {
          return 1;
        } else {
          if (hour1 < hour2) {
            return -1;
          } else if (hour1 > hour2) {
            return 1;
          } else {
            return 0;
          }
        }
      }
    }
  }

  /* Compares two Unix timestamps if they are on the same day.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareDaily(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int day1 = d1.getDayOfMonth();
    int day2 = d2.getDayOfMonth();
//    	System.out.println(day1 + " vs. " + day2);

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (day1 < day2) {
          return -1;
        } else if (day1 > day2) {
          return 1;
        } else {
          return 0;
        }
      }
    }

  }

  /**
   * Compares two Unix timestamps if they are on the same month.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareMonthly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  /**
   * Compares two Unix timestamps if they are on the same year.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareYearly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      return 0;
    }

  }
  public final static SimpleDateFormat slashesFormater = new SimpleDateFormat("MM/dd/yy"); // Used by

  /**
   * Compares two Unix timestamps if they are on the same week.
   * Tricky! Weeks, unlike months or years, can go between periods:
   *   For example: Day A can be in year 1, Day B can be in year 2, and Day A and B can be in the same week!
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareWeekly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L).withDayOfWeek(1).withMillisOfDay(0);
    DateTime d2 = new DateTime(unix2 * 1000L).withDayOfWeek(1).withMillisOfDay(0);
    return d1.compareTo(d2);
  }

  /**
   * Compares two Unix timestamps if they are on the same 15 minute.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareQuarterly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int day1 = d1.getDayOfMonth();
    int day2 = d2.getDayOfMonth();
//    	System.out.println(day1 + " vs. " + day2);

    int hour1 = d1.getHourOfDay();
    int hour2 = d2.getHourOfDay();

    int minute1 = d1.getMinuteOfHour();
    int minute2 = d2.getMinuteOfHour();

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (day1 < day2) {
          return -1;
        } else if (day1 > day2) {
          return 1;
        } else {
          if (hour1 < hour2) {
            return -1;
          } else if (hour1 > hour2) {
            return 1;
          } else {
            if (Math.abs(minute2 - minute1) <= 15) {
              return 0;
            } else if (minute1 > minute2) {
              return 1;
            } else {
              return -1;
            }
          }
        }
      }
    }
  }



}
