package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wisebison.leide.R;

public class SplashActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    findViewById(R.id.view).setOnClickListener(v -> {
      final Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    });
  }
}