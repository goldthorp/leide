package com.wisebison.leide.view;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wisebison.leide.model.EntryComponent;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

public abstract class ComponentView extends LinearLayout {

  @Getter
  private TextView nameTextView;

  @Getter
  @Setter
  private boolean valid = true;

  public ComponentView(@NonNull final Context context, final String name) {
    super(context);
    setOrientation(VERTICAL);
    if (StringUtils.isNotBlank(name)) {
      nameTextView = new TextView(context);
      nameTextView.setTypeface(null, Typeface.BOLD);
      nameTextView.setTextSize(20);
      nameTextView.setPadding(10, 3, 10, 3);
      nameTextView.setText(name);
      addView(nameTextView);
    }
  }

  abstract EntryComponent getComponent();

  void showValidationMessage() {}
}
