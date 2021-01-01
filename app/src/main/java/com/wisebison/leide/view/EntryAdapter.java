package com.wisebison.leide.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wisebison.leide.R;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryForm;
import com.wisebison.leide.util.Utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class EntryAdapter extends ArrayAdapter<EntryForm> {

  // Keep items as a field here for convenient update method
  private final ArrayList<EntryForm> items;

  private final boolean showSentiment;

  EntryAdapter(@NonNull final Context context, final ArrayList<EntryForm> items,
               final boolean showSentiment) {
    super(context, R.layout.row_diary_entry, items);
    this.items = items;
    this.showSentiment = showSentiment;
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
    final EntryForm entry = Objects.requireNonNull(getItem(position));

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
      if (showSentiment) {
         viewHolder.sentiment = customView.findViewById(R.id.sentiment_text_view);
      }
      customView.setTag(viewHolder);
    } else {
      // Reuse existing view from ViewHolder
      customView = convertView;
      viewHolder = (ViewHolder) convertView.getTag();
    }

    try {
      for (final EntryComponent component : entry.getComponents()) {
        switch (component.getType()) {
          case TEXT:
            viewHolder.text.setText(component.getValue());
            break;
          case DATE:
            final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy h:mm a", Locale.US);
            final JSONObject dateJson = new JSONObject(component.getValue());
            if (dateJson.has("timeZone") &&
              StringUtils.isNotBlank(dateJson.getString("timeZone"))) {
              sdf.setTimeZone(TimeZone.getTimeZone(dateJson.getString("timeZone")));
            }
            viewHolder.date.setText(sdf.format(new Date(dateJson.getLong("millis"))));
            break;
          case LOCATION:
            final JSONObject locationJson = new JSONObject(component.getValue());
            viewHolder.location.setText(locationJson.getString("display"));
            viewHolder.location.setVisibility(View.VISIBLE);
            break;
        }
      }
    } catch (final JSONException e) {
      e.printStackTrace();
    }

    if (showSentiment) {
      if (entry.getSentiment() != null) {
        viewHolder.sentiment.setText(
          getContext().getResources().getString(R.string.sentiment_label,
            Utils.formatFloat(entry.getSentiment(), false)));
        viewHolder.sentiment.setVisibility(View.VISIBLE);
      } else {
        viewHolder.sentiment.setVisibility(View.GONE);
      }
    }

    return customView;
  }

  void update(final List<EntryForm> entries) {
    items.clear();
    items.addAll(entries);
    notifyDataSetChanged();
  }
}
