package com.wisebison.leide.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
    final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy h:mm a", Locale.US);
    return sdf.format(millis);
  }

  //TODO
//  public static List<Tag> getTags(final float score, final float magnitude) {
//    final List<Tag> tags = new ArrayList<>();
//    if (score >= .5) {
//      if (magnitude > score) {
//        tags.add(Tag.VERY_POSITIVE);
//      } else {
//        tags.add(Tag.POSITIVE);
//      }
//    }
//    if (score >= -.1 && score <= .1 && magnitude > .1) {
//      tags.add(Tag.WELL_BALANCED);
//    }
//    if (score < -.5) {
//      tags.add(Tag.CUTS_DEEP);
//    }
//    return tags;
//  }
}
