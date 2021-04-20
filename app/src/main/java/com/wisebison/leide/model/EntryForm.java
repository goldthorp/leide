package com.wisebison.leide.model;

import android.content.Context;
import android.graphics.Typeface;
import android.text.ParcelableSpan;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import androidx.room.ColumnInfo;
import androidx.room.Relation;

import com.wisebison.leide.R;
import com.wisebison.leide.util.Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EntryForm implements Serializable {

  @ColumnInfo(name = "id")
  private Long id;

  @ColumnInfo(name = "start_timestamp")
  private Long timestamp;

  @ColumnInfo(name = "score")
  private Float sentiment;

  @ColumnInfo(name = "time_zone")
  private String timeZone;

  @Relation(parentColumn = "id", entityColumn = "entry_fk", entity = EntryComponent.class)
  private List<EntryComponentForm> components;
  
  public SpannableString getEntryForDisplay(final Context context, final boolean showSentiment) {
    final StringBuilder stringBuilder = new StringBuilder();
    final List<SpanHolder> spans = new ArrayList<>();
    for (final EntryComponentForm component : getComponents()) {
      switch (component.getType()) {
        case TEXT:
        case NUMBER:
          if (StringUtils.isNotBlank(component.getName())) {
            spans.add(getSpanHolder(stringBuilder, component.getName(),
              new StyleSpan(Typeface.BOLD)));
            stringBuilder.append(component.getName()).append(" ");
          }
          stringBuilder.append(component.getValues().get(0).getValue());
          stringBuilder.append("\n");
          break;
        case DATE:
          final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy h:mm a", Locale.US);
          sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
          final String formattedDate = sdf.format(NumberUtils.toLong(component.getValue("millis")));
          spans.add(getSpanHolder(stringBuilder, formattedDate,
            new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.3f)));
          stringBuilder.append(formattedDate);
          stringBuilder.append("\n");
          break;
        case LOCATION:
          final String locationDisplay = component.getValue("display");
          spans.add(getSpanHolder(stringBuilder, locationDisplay,
            new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f)));
          stringBuilder.append(locationDisplay);
          stringBuilder.append("\n");
          break;
      }
    }

    if (showSentiment) {
      if (getSentiment() != null) {
        final String sentiment = context.getResources().getString(R.string.sentiment_label,
          Utils.formatFloat(getSentiment(), false));
        spans.add(getSpanHolder(stringBuilder, sentiment, new StyleSpan(Typeface.BOLD)));
        stringBuilder.append(sentiment);
        stringBuilder.append("\n");
      }
    }
    stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
    final SpannableString spannableString = new SpannableString(stringBuilder);
    for (final SpanHolder spanHolder : spans) {
      for (final ParcelableSpan span : spanHolder.getSpans()) {
        spannableString.setSpan(span, spanHolder.getStart(), spanHolder.getEnd(), 0);
      }
    }
    return spannableString;
  }

  @Getter
  @AllArgsConstructor
  private static class SpanHolder {
    int start;
    int end;
    Set<ParcelableSpan> spans;
  }

  private SpanHolder getSpanHolder(
    final StringBuilder builder, final String value, final ParcelableSpan... spans) {
    final int start = builder.length();
    final int end = start + value.length();
    return new SpanHolder(start, end, new HashSet<>(Arrays.asList(spans)));
  }
}
