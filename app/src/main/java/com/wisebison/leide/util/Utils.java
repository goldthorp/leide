package com.wisebison.leide.util;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
  /**
   * Format a float to a string with one decimal place.
   *
   * @param f float to format
   * @return float as string in #.# format
   */
  public static String formatFloat(final float f, final boolean includePositiveSign) {
    final DecimalFormat df = new DecimalFormat("#.##");
    if (includePositiveSign) {
      df.setPositivePrefix("+");
    }
    return df.format(f);
  }

  /**
   * Format unix epoch millis to a readable date/time.
   *
   * @param millis to format
   * @return formatted date
   */
  public static String formatDate(final long millis) {
    return formatDate(millis, null);
  }

  /**
   * Format unix epoch millis to a readable date/time.
   *
   * @param millis    to format
   * @param timeZone  (optional) timezone for formatting
   * @return formatted date
   */
  public static String formatDate(final long millis, final String timeZone) {
    final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy h:mm a", Locale.US);
    if (StringUtils.isNotBlank(timeZone)) {
      sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
    }
    return sdf.format(millis);
  }
}
