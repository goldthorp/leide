package com.wisebison.leide.view;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wisebison.leide.R;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.model.EntryComponentValue;

import lombok.Getter;

public class CreateTextComponentView extends ConstraintLayout implements ComponentView {

  @Getter
  private final EditText textEditText;

  public CreateTextComponentView(@NonNull final Context context) {
    super(context);
    inflate(context, R.layout.view_create_text_component, this);
    textEditText = findViewById(R.id.text_edit_text);
  }

  @Override
  public EntryComponent getComponent() {
    final EntryComponent component = new EntryComponent(EntryComponentType.TEXT);
    component.getValues().add(new EntryComponentValue(textEditText.getText().toString()));
    return component;
  }
}
