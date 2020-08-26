package com.wisebison.leide.view;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisebison.leide.R;

import lombok.Getter;

@Getter
public class ModuleOptionView extends LinearLayout {

  private final TextView textView;
  private final Button addButton;

  public ModuleOptionView(final Context context) {
    super(context);
    inflate(context, R.layout.view_purchase_option, this);
    textView = findViewById(R.id.purchase_option_title);
    addButton = findViewById(R.id.add_button);
  }
}
