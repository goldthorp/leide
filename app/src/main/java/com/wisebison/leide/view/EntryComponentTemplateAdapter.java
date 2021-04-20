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
import com.wisebison.leide.model.EntryComponentTemplateForm;

import java.util.ArrayList;
import java.util.Objects;

public class EntryComponentTemplateAdapter extends ArrayAdapter<EntryComponentTemplateForm> {

  public EntryComponentTemplateAdapter(
    @NonNull final Context context,
    @NonNull final ArrayList<EntryComponentTemplateForm> templates) {
    super(context, R.layout.row_entry_component_template, templates);
  }

  // Use view holder pattern for smoother scrolling
  private static class ViewHolder {
    TextView name;
  }

  @NonNull
  @Override
  public View getView(
    final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
    final EntryComponentTemplateForm template = Objects.requireNonNull(getItem(position));
    final View customView;
    final ViewHolder viewHolder;
    if (convertView == null) {
      final LayoutInflater inflater = LayoutInflater.from(getContext());
      customView = inflater.inflate(R.layout.row_entry_component_template, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.name = customView.findViewById(R.id.template_name_text_view);
      customView.setTag(viewHolder);
    } else {
      customView = convertView;
      viewHolder = (ViewHolder) convertView.getTag();
    }

    viewHolder.name.setText(template.getName());
    return customView;
  }

  @Override
  public View getDropDownView(
    final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
    return getView(position, convertView, parent);
  }
}
