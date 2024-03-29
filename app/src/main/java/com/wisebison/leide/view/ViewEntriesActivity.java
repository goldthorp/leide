package com.wisebison.leide.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wisebison.leide.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewEntriesActivity extends AppCompatActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_entries);
  }

  // Make the action bar back button navigate back
  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }
}
