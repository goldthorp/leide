package com.wisebison.leide.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wisebison.leide.R;

import java.util.Objects;

public class ViewDiaryActivity extends AppCompatActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_diary);
    // Show back button
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
  }

  // Make the action bar back button navigate back
  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }
}
