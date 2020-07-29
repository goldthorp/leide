package com.goldthorp.wisebison.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.goldthorp.wisebison.R;
import com.goldthorp.wisebison.data.AppDatabase;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    AppDatabase.getInstance(this);
  }
}
