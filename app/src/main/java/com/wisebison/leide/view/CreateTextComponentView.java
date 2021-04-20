package com.wisebison.leide.view;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.wisebison.leide.R;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.model.EntryComponentValue;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public class CreateTextComponentView extends ComponentView {

  @Getter
  private final EditText textEditText;

  public CreateTextComponentView(@NonNull final Context context) {
    this(context, null);
  }

  public CreateTextComponentView(@NonNull final Context context, final String name) {
    super(context, name);
    inflate(context, R.layout.view_create_text_component, this);
    textEditText = findViewById(R.id.text_edit_text);
  }

  @Override
  public EntryComponent getComponent() {
    final EntryComponent component = new EntryComponent(EntryComponentType.TEXT);
    if (getNameTextView() != null && StringUtils.isNotBlank(getNameTextView().getText())) {
      component.setName(getNameTextView().getText().toString());
    }
    component.getValues().add(new EntryComponentValue(textEditText.getText().toString()));
    return component;
  }
}
