package com.wisebison.leide.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wisebison.leide.R;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.model.EntryComponentValue;
import com.wisebison.leide.util.Utils;

import java.util.Calendar;

public class CreateDateComponentView extends ComponentView{

  private long dateMillis;

  private final TextView dateTextView;

  public CreateDateComponentView(@NonNull final Context context) {
    this(context, null);
  }

  public CreateDateComponentView(@NonNull final Context context, final String name) {
    super(context, name);

    inflate(context, R.layout.view_create_date_component, this);
    dateTextView = findViewById(R.id.date_text_view);
    setDateMillis(System.currentTimeMillis());

    final ImageView edit = findViewById(R.id.edit_date);

    edit.setOnClickListener(v -> {
      final Calendar currentDate = Calendar.getInstance();
      final Calendar date = Calendar.getInstance();
      new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
        date.set(year, monthOfYear, dayOfMonth);
        new TimePickerDialog(context, (view1, hourOfDay, minute) -> {
          date.set(Calendar.HOUR_OF_DAY, hourOfDay);
          date.set(Calendar.MINUTE, minute);
          setDateMillis(date.getTimeInMillis());
        }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
      }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    });
  }

  public void setDateMillis(final long dateMillis) {
    this.dateMillis = dateMillis;
    dateTextView.setText(Utils.formatDate(dateMillis));
  }

  @Override
  public EntryComponent getComponent() {
    final EntryComponent component = new EntryComponent(EntryComponentType.DATE);
    component.getValues().add(new EntryComponentValue("millis", String.valueOf(dateMillis)));
    return component;
  }
}
