package com.goldthorp.wisebison.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldthorp.wisebison.R;
import com.goldthorp.wisebison.model.DiaryEntry;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class DiaryEntryAdapter extends ArrayAdapter<DiaryEntry> {

  // Keep items as a field here for convenient update method
  private final ArrayList<DiaryEntry> items;

  DiaryEntryAdapter(@NonNull final Context context, final ArrayList<DiaryEntry> items) {
    super(context, R.layout.row_diary_entry, items);
    this.items = items;
  }

  // Use view holder pattern for smoother scrolling
  private static class ViewHolder {
    TextView date;
    TextView text;
    TextView location;
    TextView sentiment;
  }
  @NonNull
  @Override
  public View getView(final int position, @Nullable final View convertView,
                      @NonNull final ViewGroup parent) {
    final DiaryEntry entry = Objects.requireNonNull(getItem(position));

    final View customView;
    final ViewHolder viewHolder;
    if (convertView == null) {
      // View doesn't exist; create new one
      final LayoutInflater inflater = LayoutInflater.from(getContext());
      customView = inflater.inflate(R.layout.row_diary_entry, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.date = customView.findViewById(R.id.dateTextView);
      viewHolder.text = customView.findViewById(R.id.textTextView);
      viewHolder.location = customView.findViewById(R.id.locationTextView);
      // TODO
//      viewHolder.sentiment = customView.findViewById(R.id.sentiment_text_view);
      customView.setTag(viewHolder);
    } else {
      // Reuse existing view from ViewHolder
      customView = convertView;
      viewHolder = (ViewHolder) convertView.getTag();
    }

    final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy h:mm a", Locale.US);
    if (StringUtils.isNotBlank(entry.getTimeZone())) {
      sdf.setTimeZone(TimeZone.getTimeZone(entry.getTimeZone()));
    }
    viewHolder.date.setText(sdf.format(new Date(entry.getStartTimestamp())));

    viewHolder.text.setText(entry.getText());

    if (StringUtils.isNotBlank(entry.getLocation())) {
      viewHolder.location.setText(entry.getLocation());
      viewHolder.location.setVisibility(View.VISIBLE);
    } else {
      viewHolder.location.setVisibility(View.GONE);
    }

    // TODO
//    if (entry.getSentiment() != null) {
//      viewHolder.sentiment.setText("Sentiment: " + entry.getSentiment());
//      viewHolder.sentiment.setVisibility(View.VISIBLE);
//    } else {
//      viewHolder.sentiment.setVisibility(View.GONE);
//    }

    return customView;
  }

  void update(final List<DiaryEntry> entries) {
    items.clear();
    items.addAll(entries);
    notifyDataSetChanged();
  }
}
