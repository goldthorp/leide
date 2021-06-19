package com.wisebison.leide.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.maltaisn.calcdialog.CalcDialog;
import com.wisebison.leide.R;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.model.EntryComponentValue;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import lombok.Getter;

public class CreateNumberComponentView extends ComponentView {

  @Getter
  private final EditText numberEditText;
  private final TextView validationTextView;

  private final String minimum;
  private final String maximum;

  @Getter
  private final Integer idx;

  public CreateNumberComponentView(@NonNull final Context context) {
    this(context, null, null, null, null, null);
  }

  public CreateNumberComponentView(@NonNull final Context context, final String name,
                                   final String minimum, final String maximum,
                                   final CalcDialog calcDialog, final Integer idx) {
    super(context, name);
    this.idx = idx;
    setValid(false);
    this.minimum = minimum;
    this.maximum = maximum;
    if (getNameTextView() != null) {
      if (StringUtils.isNotBlank(minimum) && StringUtils.isNotBlank(maximum)) {
        getNameTextView().setText(
          getResources().getString(R.string.name_min_max_label, name, minimum, maximum));
      } else if (StringUtils.isNotBlank(minimum)) {
        getNameTextView().setText(
          getResources().getString(R.string.name_min_label, name, minimum));
      } else if (StringUtils.isNotBlank(maximum)) {
        getNameTextView().setText(
          getResources().getString(R.string.name_max_label, name, maximum));
      }
    }
    inflate(context, R.layout.view_create_number_component, this);
    numberEditText = findViewById(R.id.number_edit_text);

    validationTextView = findViewById(R.id.number_component_validation_message_text_view);
    numberEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(
        final CharSequence s, final int start, final int count, final int after) {}

      @Override
      public void onTextChanged(
        final CharSequence s, final int start, final int before, final int count) {}

      @Override
      public void afterTextChanged(final Editable s) {
        handleValidation(s.toString());
      }
    });

    findViewById(R.id.calc_icon).setOnClickListener(v -> {
      calcDialog.show(((AppCompatActivity)getContext()).getSupportFragmentManager(),
        CreateNumberComponentView.class.getSimpleName());
    });
  }

  private void handleValidation(final String valueStr) {
    try {
      if (StringUtils.isNotBlank(valueStr) &&
        (StringUtils.isNotBlank(minimum) || StringUtils.isNotBlank(maximum))) {
        final float value = Float.parseFloat(valueStr);
        if (StringUtils.isNotBlank(minimum)) {
          if (value < Integer.parseInt(minimum)) {
            validationTextView.setText(
              getContext().getString(R.string.number_component_value_minimum_validation, minimum));
            validationTextView.setVisibility(VISIBLE);
            setValid(false);
          } else {
            validationTextView.setVisibility(GONE);
            setValid(true);
          }
        }
        if (validationTextView.getVisibility() != VISIBLE && StringUtils.isNotBlank(maximum)) {
          if (value > Integer.parseInt(maximum)) {
            validationTextView.setText(
              getContext().getString(R.string.number_component_value_maximum_validation, maximum));
            validationTextView.setVisibility(VISIBLE);
            setValid(false);
          } else {
            validationTextView.setVisibility(GONE);
            setValid(true);
          }
        }
      } else if (StringUtils.isBlank(valueStr)) {
        validationTextView.setText(getContext().getString(R.string.validation_required));
        validationTextView.setVisibility(VISIBLE);
        setValid(false);
      } else {
        validationTextView.setVisibility(GONE);
        setValid(true);
      }
    } catch (final NumberFormatException nfe) {
      validationTextView.setText(getContext().getString(R.string.validation_number_required));
      validationTextView.setVisibility(VISIBLE);
      setValid(false);
    }
  }

  @Override
  EntryComponent getComponent() {
    final EntryComponent component = new EntryComponent(EntryComponentType.NUMBER);
    if (getNameTextView() != null && StringUtils.isNotBlank(getNameTextView().getText())) {
      component.setName(getNameTextView().getText().toString());
    }
    final DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    df.setMaximumFractionDigits(6);
    component.getValues().add(new EntryComponentValue(
      df.format(Double.parseDouble(numberEditText.getText().toString()))));
    return component;
  }

  @Override
  void showValidationMessage() {
    handleValidation(numberEditText.getText().toString());
  }
}
