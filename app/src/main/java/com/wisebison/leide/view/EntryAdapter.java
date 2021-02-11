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
import com.wisebison.leide.model.EntryForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    TextView textView;
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
      viewHolder.textView = customView.findViewById(R.id.entry_text_view);

      customView.setTag(viewHolder);
    } else {
      // Reuse existing view from ViewHolder
      customView = convertView;
      viewHolder = (ViewHolder) convertView.getTag();
    }

    viewHolder.textView.setText(entry.getEntryForDisplay(getContext(), showSentiment));

    return customView;
  }

  void update(final List<EntryForm> entries) {
    items.clear();
    items.addAll(entries);
    notifyDataSetChanged();
  }
}
