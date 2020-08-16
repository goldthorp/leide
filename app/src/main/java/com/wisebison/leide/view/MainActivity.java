package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wisebison.leide.R;
import com.wisebison.leide.data.BackupUtil;
import com.wisebison.leide.view.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

  private static final int LOGIN_REQUEST_CODE = 1;

  private BackupUtil backupUtil;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Check if user is already signed in to firebase. If so, start backup immediately. If not,
    // start login activity and start backup once authentication is complete
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser == null) {
      final Intent intent = new Intent(this, LoginActivity.class);
      startActivityForResult(intent, LOGIN_REQUEST_CODE);
    } else {
      backupUtil = new BackupUtil(this);
    }
  }

  // Start the backup once user is logged in
  @Override
  protected void onActivityResult(
    final int requestCode, final int resultCode, @Nullable final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
      backupUtil = new BackupUtil(this);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    final int id = item.getItemId();

    if (id == R.id.action_settings && backupUtil != null) {
      backupUtil.restore();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
