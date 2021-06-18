package com.wisebison.leide.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wisebison.leide.R;
import com.wisebison.leide.util.Utils;

import java.util.Calendar;

import lombok.Getter;

public class CreateDateView extends LinearLayout {

  @Getter
  private long dateMillis;

  private final TextView dateTextView;

  public CreateDateView(@NonNull final Context context) {
    super(context);
    setOrientation(VERTICAL);
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

}
