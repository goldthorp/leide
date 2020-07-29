package com.goldthorp.wisebison.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.goldthorp.wisebison.R;
import com.goldthorp.wisebison.data.AppDatabase;
import com.goldthorp.wisebison.view.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

  private static final int LOGIN_REQUEST_CODE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Check if user is already signed in to firebase. If so, start backup immediately. If not,
    // start login activity and start backup once authentication is complete
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser == null) {
      final Intent intent = new Intent(this, LoginActivity.class);
      startActivityForResult(intent, LOGIN_REQUEST_CODE);
    } else {
//      Backup.start(this);
    }
  }

  // Start the backup once user is logged in
  @Override
  protected void onActivityResult(
    final int requestCode, final int resultCode, @Nullable final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
//      Backup.start(this);
    }
  }
}
