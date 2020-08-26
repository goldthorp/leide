package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wisebison.leide.R;
import com.wisebison.leide.billing.BillingUtil;
import com.wisebison.leide.data.BackupUtil;
import com.wisebison.leide.model.ModuleType;
import com.wisebison.leide.view.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

  private static final int LOGIN_REQUEST_CODE = 1;

  private BackupUtil backupUtil;

  private BillingUtil billingUtil;

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

    addModules(new DiaryModuleFragment());

    billingUtil = new BillingUtil(this);
  }

  private void addModules(final Fragment... fragments) {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    for (final Fragment fragment : fragments) {
      fragmentTransaction.add(R.id.module_container, fragment);
    }
    fragmentTransaction.commit();
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

    if (id == R.id.action_restore_backup && backupUtil != null) {
      backupUtil.restore();
      return true;
    }

    if (id == R.id.action_add_module) {
      final LinearLayout dialogLayout = new LinearLayout(this);
      dialogLayout.setOrientation(LinearLayout.VERTICAL);
      for (final ModuleType moduleType : ModuleType.values()) {
        final ModuleOptionView moduleOptionView = new ModuleOptionView(this);
        moduleOptionView.getTextView().setText(moduleType.getTitleId());
        if (moduleType.getSku() == null || billingUtil.isPurchased(moduleType.getSku())) {
          moduleOptionView.getAddButton().setText(getString(R.string.add));
          moduleOptionView.getAddButton().setOnClickListener(v -> onAddModule(moduleType));
        } else {
          moduleOptionView.getAddButton().setText(billingUtil.getPrice(moduleType.getSku()));
          moduleOptionView.getAddButton().setOnClickListener(view -> {
            billingUtil.purchase(moduleType.getSku(), () -> onAddModule(moduleType));
          });
        }
        dialogLayout.addView(moduleOptionView);
      }
      new AlertDialog.Builder(this)
        .setView(dialogLayout)
        .show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void onAddModule(final ModuleType moduleType) {
    Log.d("TEST", "add " + moduleType + " to layout");
  }
}
