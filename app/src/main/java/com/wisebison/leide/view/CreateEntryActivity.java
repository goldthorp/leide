package com.wisebison.leide.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryComponentTemplateDao;
import com.wisebison.leide.data.EntryDao;
import com.wisebison.leide.model.Entry;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentTemplate;
import com.wisebison.leide.model.EntryComponentType;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class CreateEntryActivity extends AppCompatActivity {

  private static final String TAG = CreateEntryActivity.class.getSimpleName();

  private long startTimestamp;

  private LinearLayout componentContainer;
  private EntryComponentTemplateAdapter templateAdapter;

  private ArrayList<EntryComponentTemplate> templates;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_entry);

    startTimestamp = System.currentTimeMillis();

    final EntryComponentTemplateDao templateDao =
      AppDatabase.getInstance(this).getEntryComponentTemplateDao();

    componentContainer = findViewById(R.id.component_container_layout);

    final AtomicReference<AlertDialog> templateDialog = new AtomicReference<>();

    final ImageView addComponent = findViewById(R.id.add_component);

    templates = new ArrayList<>();

    templateDao.getAll().observe(this, templates -> {
      this.templates.addAll(templates);
      final LinearLayout dialogLayout = new LinearLayout(this);
      getLayoutInflater().inflate(R.layout.dialog_component_template, dialogLayout);
      final Spinner templateSpinner = dialogLayout.findViewById(R.id.template_spinner);
      templateAdapter = new EntryComponentTemplateAdapter(this, this.templates);
      templateSpinner.setAdapter(templateAdapter);
      for (final EntryComponentTemplate template : this.templates) {
        if (template.getType() == EntryComponentType.DATE) {
          addComponent(template, -1);
          break;
        }
      }
      templateDialog.set(new AlertDialog.Builder(this)
        .setTitle("Select a component")
        .setView(dialogLayout)
        .setPositiveButton("Submit", ((dialog, which) -> {
          final int addComponentIndex = componentContainer.indexOfChild(addComponent);
          final EntryComponentTemplate selectedComponent =
            (EntryComponentTemplate) templateSpinner.getSelectedItem();
          addComponent(selectedComponent, addComponentIndex);
        })).create());
    });

    addComponent.setOnClickListener(v -> {
      if (templateDialog.get() != null) {
        templateDialog.get().show();
      }
    });

    final Button saveButton = findViewById(R.id.save_button);
    saveButton.setOnClickListener(v -> saveEntry());
  }

  private void addComponent(final EntryComponentTemplate componentTemplate, final int index) {
    switch (componentTemplate.getType()) {
      case DATE:
        componentContainer.addView(new CreateDateComponentView(this), index);
        break;
      case LOCATION:
        componentContainer.addView(new CreateLocationComponentView(this), index);
        break;
      case TEXT:
        componentContainer.addView(new CreateTextComponentView(this), index);
        break;
      default:
        Log.e(TAG, "Unsupported entry component type " + componentTemplate.getType());
    }
    if (componentTemplate.getType().isOnlyOnePerEntry()) {
      for (final EntryComponentTemplate template : templates) {
        if (template.getType() == componentTemplate.getType()) {
          templates.remove(template);
          break;
        }
      }
    }
    templateAdapter.notifyDataSetChanged();
  }

  // Display a confirm dialog before exiting on back-press
  @Override
  public void onBackPressed() {
    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.confirm_discard_title))
        .setMessage(getString(R.string.confirm_discard_message))
        .setPositiveButton("Yes", (dialog, which) -> finish())
        .setNegativeButton("No", null)
        .show();
  }

  private void saveEntry() {
    final Entry entry = new Entry();
    entry.setStartTimestamp(startTimestamp);
    entry.setSaveTimestamp(System.currentTimeMillis());
    entry.setTimeZone(TimeZone.getDefault().getID());

    for (int i = 0; i < componentContainer.getChildCount(); i++) {
      final ComponentView componentView = (ComponentView) componentContainer.getChildAt(i);
      final EntryComponent component = componentView.getComponent();
      component.setListSeq(i);
      entry.getComponents().add(component);
    }
    // Insert on a separate thread because otherwise room throws an error
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final EntryDao entryDao = AppDatabase.getInstance(this).getEntryDao();
    try {
      executorService.submit(() -> entryDao.insert(entry)).get();
      finish();
    } catch (final ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
