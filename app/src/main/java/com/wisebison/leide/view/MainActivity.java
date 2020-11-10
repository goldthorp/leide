package com.wisebison.leide.view;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
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

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {

  private static final String TAG = "MainActivity";

  private static final int LOGIN_REQUEST_CODE = 1;

  private BackupUtil backupUtil;

  public BillingUtil billingUtil;

  private AnalyzeUtil analyzeUtil;

  private ModuleDao moduleDao;

  private Map<ModuleType, Pair<Module, ModuleFragment>> addedModules;
  private List<Module> allModules;

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
      onLoggedIn();
    }

    addedModules = new HashMap<>();
    allModules = new ArrayList<>();

    moduleDao = AppDatabase.getInstance(this).getModuleDao();

    analyzeUtil = AnalyzeUtil.getInstance(this);

    ImageView add = findViewById(R.id.add);
    add.setOnClickListener(v -> {
      final LinearLayout dialogLayout = new LinearLayout(this);
      dialogLayout.setOrientation(LinearLayout.VERTICAL);
      final List<Button> addButtons = new ArrayList<>();
      final boolean userHasPremium = billingUtil.hasPremium();
      for (final ModuleType moduleType : ModuleType.values()) {
        final ModuleOptionView moduleOptionView = new ModuleOptionView(this);
        moduleOptionView.getTextView().setText(moduleType.getTitleId());
        moduleOptionView.getAddButton().setText(getString(R.string.add));
        if (!moduleType.isPremium() || userHasPremium) {
          moduleOptionView.getAddButton().setOnClickListener(b -> onAddModule(moduleType));
        } else {
          moduleOptionView.getAddButton().setEnabled(false);
          addButtons.add(moduleOptionView.getAddButton());
        }
        dialogLayout.addView(moduleOptionView);
      }
      if (!userHasPremium) {
        final Button subscribeButton = new Button(this);
        subscribeButton.setText(R.string.subscribe);
        subscribeButton.setOnClickListener(view ->
          billingUtil.purchase("pro_3_month", () -> {
            subscribeButton.setVisibility(View.GONE);
            for (final Button addButton : addButtons) {
              addButton.setEnabled(true);
            }
            addModulesToView();
            analyzeUtil.start();
          }));
        dialogLayout.addView(subscribeButton);
      }
      new AlertDialog.Builder(this)
        .setView(dialogLayout)
        .show();
    });
    // TODO remove
    add.setOnLongClickListener(v -> {
      if (backupUtil != null) {
        backupUtil.restore();
        return true;
      }
      return false;
    });

    ImageView menu = findViewById(R.id.menu);
    menu.setOnClickListener(v -> {
      final Intent intent = new Intent(this, MenuActivity.class);
      startActivity(intent, ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in,
        android.R.anim.fade_out).toBundle());
    });

    FloatingActionButton newEntryFab = findViewById(R.id.new_entry_fab);
    newEntryFab.setOnClickListener(v -> {
      final Intent intent = new Intent(this, CreateDiaryEntryActivity.class);
      startActivity(intent);
    });
  }

  private void addModule(final Fragment fragment) {
    Fragment existingFragment =
      getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
    if (existingFragment == null) {
      final FragmentManager fragmentManager = getSupportFragmentManager();
      final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.add(R.id.module_container, fragment, fragment.getClass().getSimpleName());
      fragmentTransaction.commit();
    }
  }

  // Start the backup once user is logged in
  @Override
  protected void onActivityResult(
    final int requestCode, final int resultCode, @Nullable final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
      onLoggedIn();
    }
  }

  private void onLoggedIn() {
    backupUtil = new BackupUtil(this);
    backupUtil.start();
    billingUtil = new BillingUtil(this);
    billingUtil.setOnBillingUtilReadyListener(() -> {
      if (billingUtil.hasPremium()) {
        analyzeUtil.start();
      }
      moduleDao.getAll().observe(this, modules -> {
        allModules.clear();
        allModules.addAll(modules);
        addModulesToView();
      });
    });
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  private void addModulesToView() {
    final List<ModuleType> currentModules = new ArrayList<>();
    for (final Module module : allModules) {
      final ModuleFragment fragment = module.getFragment();
      currentModules.add(module.getModuleType());
      if (fragment != null) {
        if (!addedModules.containsKey(module.getModuleType())) {
          if (!module.getModuleType().isPremium() || billingUtil.hasPremium()) {
            addModule(fragment);
            addedModules.put(module.getModuleType(), Pair.create(module, fragment));
          }
        } else if (
          addedModules.get(module.getModuleType()).second.getColor() != module.getColor()) {
          addedModules.get(module.getModuleType()).second.setColor(module.getColor());
        }
      }
    }
    final Iterator<ModuleType> iterator = addedModules.keySet().iterator();
    while (iterator.hasNext()) {
      final ModuleType addedModule = iterator.next();
      if (!currentModules.contains(addedModule)) {
        getSupportFragmentManager().beginTransaction()
          .remove(Objects.requireNonNull(addedModules.get(addedModule).second)).commit();
        iterator.remove();
      }
    }
  }

  private void onAddModule(final ModuleType moduleType) {
    if (!addedModules.containsKey(moduleType)) {
      final Module module = new Module(moduleType);
      BackgroundUtil.doInBackgroundNow(() -> moduleDao.insert(module));
    }
  }

  @Override
  public void onColorSelected(int dialogId, int color) {
    Module module = addedModules.get(ModuleType.values()[dialogId]).first;
    module.setColor(color);
    BackgroundUtil.doInBackgroundNow(() -> moduleDao.update(module));
  }

  @Override
  public void onDialogDismissed(int dialogId) { }
}
