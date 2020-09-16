package com.wisebison.leide.view;

import android.content.Intent;
import android.os.Bundle;
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
import com.wisebison.leide.cloud.AnalyzeUtil;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.BackupUtil;
import com.wisebison.leide.data.ModuleDao;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.model.ModuleType;
import com.wisebison.leide.util.BackgroundUtil;
import com.wisebison.leide.view.auth.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final int LOGIN_REQUEST_CODE = 1;

  private BackupUtil backupUtil;

  private BillingUtil billingUtil;

  private ModuleDao moduleDao;

  private Map<ModuleType, ModuleFragment> addedModules;

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

    addedModules = new HashMap<>();

    final AnalyzeUtil analyzeUtil = AnalyzeUtil.getInstance(this);

    moduleDao = AppDatabase.getInstance(this).getModuleDao();
    moduleDao.getAll().observe(this, modules -> {
      final List<ModuleType> currentModules = new ArrayList<>();
      for (final Module module : modules) {
        final ModuleFragment fragment = module.getFragment();
        if (fragment != null && !addedModules.containsKey(module.getModuleType())) {
          addModules(fragment);
          addedModules.put(module.getModuleType(), fragment);
          currentModules.add(module.getModuleType());
          if (ModuleType.NAMED_ENTITIES.equals(module.getModuleType())) {
            analyzeUtil.setHasEntitiesModule(true);
            analyzeUtil.start();
          }
        }
      }
      final Iterator<ModuleType> iterator = addedModules.keySet().iterator();
      while (iterator.hasNext()) {
        final ModuleType addedModule = iterator.next();
        if (!currentModules.contains(addedModule)) {
          getSupportFragmentManager().beginTransaction()
            .remove(Objects.requireNonNull(addedModules.get(addedModule))).commit();
          iterator.remove();
          if (ModuleType.NAMED_ENTITIES.equals(addedModule)) {
            analyzeUtil.setHasEntitiesModule(false);
          }
        }
      }
    });

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
    if (!addedModules.containsKey(moduleType)) {
      final Module module = new Module(moduleType);
      BackgroundUtil.doInBackgroundNow(() -> moduleDao.insert(module));
    }
  }
}
