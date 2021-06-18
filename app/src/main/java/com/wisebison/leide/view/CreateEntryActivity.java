package com.wisebison.leide.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.maltaisn.calcdialog.CalcDialog;
import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryComponentTemplateDao;
import com.wisebison.leide.data.EntryDao;
import com.wisebison.leide.model.Entry;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentSetting;
import com.wisebison.leide.model.EntryComponentTemplate;
import com.wisebison.leide.model.EntryComponentTemplateForm;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.util.ArrayAdapterItem;
import com.wisebison.leide.util.BackgroundUtil;
import com.wisebison.leide.util.GetLocationTask;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateEntryActivity extends AppCompatActivity implements CalcDialog.CalcDialogCallback {

  private static final String TAG = CreateEntryActivity.class.getSimpleName();

  private long startTimestamp;

  private LinearLayout componentContainer;
  private EntryComponentTemplateAdapter templateAdapter;

  private ArrayList<EntryComponentTemplateForm> templates;

  private int calcDialogIdx;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_entry);

    startTimestamp = System.currentTimeMillis();

    final EntryComponentTemplateDao templateDao =
      AppDatabase.getInstance(this).getEntryComponentTemplateDao();

    componentContainer = findViewById(R.id.component_container_layout);

    final AlertDialog templateDialog = new AlertDialog.Builder(this)
      .setTitle(R.string.select_component)
      .create();

    final AlertDialog newComponentDialog = new AlertDialog.Builder(this)
      .setTitle(getString(R.string.create_new_component))
      .setPositiveButton(R.string.submit, (dialog, which) -> {
        final AlertDialog alertDialog = (AlertDialog) dialog;
        final Spinner componentValueTypeSpinner =
          Objects.requireNonNull(alertDialog.findViewById(R.id.component_value_type_spinner));
        @SuppressWarnings("rawtypes") final EntryComponentType type = (EntryComponentType)
            ((ArrayAdapterItem) componentValueTypeSpinner.getSelectedItem()).getId();
        final EditText nameEditText =
          Objects.requireNonNull(alertDialog.findViewById(R.id.new_component_name_edit_text));
        final CheckBox futureReuseCheckbox =
          Objects.requireNonNull(alertDialog.findViewById(R.id.future_reuse_checkbox));
        final CheckBox displayNameInEntryCheckbox =
          Objects.requireNonNull(alertDialog.findViewById(R.id.display_name_in_entry_checkbox));
        final Map<String, String> settings = new HashMap<>();
        settings.put("displayNameInEntry",
          Boolean.toString(!futureReuseCheckbox.isChecked() || displayNameInEntryCheckbox.isChecked()));
        if (type == EntryComponentType.NUMBER) {
          final EditText minimumEditText = Objects.requireNonNull(
            alertDialog.findViewById(R.id.number_component_minimum_edit_text));
          final EditText maximumEditText = Objects.requireNonNull(
            alertDialog.findViewById(R.id.number_component_maximum_edit_text));
          if (StringUtils.isNotBlank(minimumEditText.getText())) {
            settings.put("minimum", minimumEditText.getText().toString());
          }
          if (StringUtils.isNotBlank(maximumEditText.getText())) {
            settings.put("maximum", maximumEditText.getText().toString());
          }
        }
        addComponent(type, nameEditText.getText().toString(), settings,  -1);
        if (futureReuseCheckbox.isChecked()) {
          final EntryComponentTemplate template = new EntryComponentTemplate();
          template.setType(type);
          template.setName(nameEditText.getText().toString());
          template.getSettings().add(new EntryComponentSetting("displayNameInEntry",
              String.valueOf(displayNameInEntryCheckbox.isChecked())));
          if (type == EntryComponentType.NUMBER) {
            final EditText minimumEditText = Objects.requireNonNull(
              alertDialog.findViewById(R.id.number_component_minimum_edit_text));
            final EditText maximumEditText = Objects.requireNonNull(
              alertDialog.findViewById(R.id.number_component_maximum_edit_text));
            if (StringUtils.isNotBlank(minimumEditText.getText())) {
              template.getSettings().add(new EntryComponentSetting("minimum",
                minimumEditText.getText().toString()));
            }
            if (StringUtils.isNotBlank(maximumEditText.getText())) {
              template.getSettings().add(new EntryComponentSetting("maximum",
                maximumEditText.getText().toString()));
            }
          }
          BackgroundUtil.doInBackgroundNow(() -> templateDao.insert(template));
        }
      })
      .create();

    final ImageView addComponent = findViewById(R.id.add_component);

    templates = new ArrayList<>();
    componentContainer.addView(new CreateDateView(this));
    templateDao.getAll().observe(this, templates -> {
      this.templates.clear();
      this.templates.addAll(templates);

      final LinearLayout dialogLayout = new LinearLayout(this);
      getLayoutInflater().inflate(R.layout.dialog_component_template, dialogLayout);
      final ListView templateListView = dialogLayout.findViewById(R.id.template_list_view);
      templateAdapter = new EntryComponentTemplateAdapter(this, this.templates);
      templateListView.setAdapter(templateAdapter);

      templateListView.setOnItemClickListener((parent, view, position, id) -> {
        final int addComponentIndex = componentContainer.indexOfChild(addComponent);
        final EntryComponentTemplateForm selectedComponent = templateAdapter.getItem(position);
        addComponent(selectedComponent.getType(), selectedComponent.getName(),
          selectedComponent.getSettingsAsMap(), addComponentIndex);
        templateDialog.dismiss();
      });

      templateDialog.setView(dialogLayout);
      for (final EntryComponentTemplateForm template : this.templates) {
        if (template.getType() == EntryComponentType.LOCATION) {
          addComponent(template.getType(), template.getName(), template.getSettingsAsMap(), -1);
          break;
        }
      }

      final LinearLayout newComponentOption = dialogLayout.findViewById(R.id.new_component_option);
      final LinearLayout newComponentDialogLayout = new LinearLayout(this);
      getLayoutInflater().inflate(R.layout.dialog_create_new_component, newComponentDialogLayout);
      newComponentOption.setOnClickListener(v -> {
        newComponentDialog.show();
        templateDialog.dismiss();

        final Spinner componentValueTypeSpinner =
          newComponentDialogLayout.findViewById(R.id.component_value_type_spinner);
        final ArrayAdapter<ArrayAdapterItem<EntryComponentType>> componentValueTypeAdapter =
          new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            Arrays.asList(
              new ArrayAdapterItem<>(EntryComponentType.TEXT, getString(R.string.text)),
              new ArrayAdapterItem<>(EntryComponentType.NUMBER, getString(R.string.number)),
              new ArrayAdapterItem<>(null, getString(R.string.no_value))));
        componentValueTypeSpinner.setAdapter(componentValueTypeAdapter);

        componentValueTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(
            final AdapterView<?> parent, final View view, final int position, final long id) {
            setNewComponentDialogState(newComponentDialogLayout);
          }

          @Override
          public void onNothingSelected(final AdapterView<?> parent) { }
        });

        final CheckBox futureReuseCheckbox =
          newComponentDialogLayout.findViewById(R.id.future_reuse_checkbox);
        futureReuseCheckbox.setOnClickListener(cb -> {
          setNewComponentDialogState(newComponentDialogLayout);
        });
      });
      newComponentDialog.setView(newComponentDialogLayout);
    });

    addComponent.setOnClickListener(v -> {
      templateDialog.show();
    });

    final Button saveButton = findViewById(R.id.save_button);
    saveButton.setOnClickListener(v -> saveEntry());
  }

  private void setNewComponentDialogState(
    final LinearLayout newComponentDialogLayout) {
    final EditText nameEditText =
      newComponentDialogLayout.findViewById(R.id.new_component_name_edit_text);

    final LinearLayout displayNameInEntryLayout =
      newComponentDialogLayout.findViewById(R.id.display_name_in_entry_layout);

    final CheckBox displayNameInEntryCheckbox =
      newComponentDialogLayout.findViewById(R.id.display_name_in_entry_checkbox);

    final CheckBox futureReuseCheckbox =
      newComponentDialogLayout.findViewById(R.id.future_reuse_checkbox);

    final Spinner componentValueTypeSpinner =
      newComponentDialogLayout.findViewById(R.id.component_value_type_spinner);

    @SuppressWarnings("rawtypes") final boolean noValue =
      ((ArrayAdapterItem) componentValueTypeSpinner.getSelectedItem()).getId() == null;
    final boolean nameRequired = futureReuseCheckbox.isChecked() || noValue;
    if (nameRequired) {
      nameEditText.setHint(R.string.name_required);
      displayNameInEntryLayout.setVisibility(View.VISIBLE);
    } else {
      nameEditText.setHint(R.string.name_optional);
      displayNameInEntryLayout.setVisibility(View.GONE);
    }
    if (noValue) {
      displayNameInEntryCheckbox.setEnabled(false);
      displayNameInEntryCheckbox.setChecked(true);
    } else {
      displayNameInEntryCheckbox.setEnabled(true);
    }

    //noinspection rawtypes
    if (((ArrayAdapterItem) componentValueTypeSpinner.getSelectedItem()).getId() ==
      EntryComponentType.NUMBER) {
      newComponentDialogLayout.findViewById(R.id.number_component_min_max_layout)
        .setVisibility(View.VISIBLE);
    } else {
      newComponentDialogLayout.findViewById(R.id.number_component_min_max_layout)
        .setVisibility(View.GONE);
    }
  }

  private void addComponent(
    final EntryComponentType componentType, final String componentName,
    final Map<String, String> settings, final int index) {
    final String name =
      Boolean.parseBoolean(settings.get("displayNameInEntry")) ? componentName : null;
    if (componentType == null) {
      if (StringUtils.isBlank(name)) {
        throw new IllegalArgumentException("Name must be specified for components with no type");
      }
      componentContainer.addView(new ComponentView(this, name) {
        @Override
        EntryComponent getComponent() {
          return new EntryComponent(name);
        }
      }, index);
      return;
    }
    if (componentType.isOnlyOnePerEntry()) {
      for (int i = 0; i < componentContainer.getChildCount(); i++) {
        if (componentContainer.getChildAt(i) instanceof ComponentView) {
          final ComponentView componentView = (ComponentView) componentContainer.getChildAt(i);
          if (componentView.getComponent().getType() == componentType) {
            return;
          }
        }
      }
    }
    switch (componentType) {
      case LOCATION:
        componentContainer.addView(new CreateLocationComponentView(this, name), index);
        break;
      case TEXT:
        componentContainer.addView(new CreateTextComponentView(this, name), index);
        break;
      case NUMBER:
        final CalcDialog calcDialog = new CalcDialog();
        calcDialog.getSettings().setExpressionShown(true);
        calcDialog.getSettings().setExpressionEditable(true);
        calcDialog.getSettings().setRequestCode(calcDialogIdx);
        componentContainer.addView(new CreateNumberComponentView(this, name,
          settings.get("minimum"), settings.get("maximum"), calcDialog, calcDialogIdx), index);
        calcDialogIdx++;
        break;
      default:
        Log.e(TAG, "Unsupported entry component type " + componentType);
    }
    if (componentType.isOnlyOnePerEntry()) {
      for (final EntryComponentTemplateForm template : templates) {
        if (template.getType() == componentType) {
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

  private boolean formIsValid() {
    boolean valid = true;
    for (int i = 0; i < componentContainer.getChildCount(); i++) {
      if (componentContainer.getChildAt(i) instanceof ComponentView) {
        final ComponentView componentView = (ComponentView) componentContainer.getChildAt(i);
        if (!componentView.isValid()) {
          valid = false;
          componentView.showValidationMessage();
        }
      }
    }
    return valid;
  }

  private void saveEntry() {
    if (!formIsValid()) {
      return;
    }
    final Entry entry = new Entry();
    entry.setStartTimestamp(startTimestamp);
    entry.setSaveTimestamp(System.currentTimeMillis());
    entry.setTimeZone(TimeZone.getDefault().getID());
    final CreateDateView dateView = (CreateDateView) componentContainer.getChildAt(0);
    entry.setDisplayTimestamp(dateView.getDateMillis());

    for (int i = 0; i < componentContainer.getChildCount(); i++) {
      if (componentContainer.getChildAt(i) instanceof ComponentView) {
        final ComponentView componentView = (ComponentView) componentContainer.getChildAt(i);
        final EntryComponent component = componentView.getComponent();
        component.setListSeq(i);
        entry.getComponents().add(component);
      }
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

  @Override
  public void onValueEntered(final int requestCode, @Nullable final BigDecimal value) {
    for (int i = 0; i < componentContainer.getChildCount(); i++) {
      final View componentView = componentContainer.getChildAt(i);
      if (componentView instanceof CreateNumberComponentView) {
        final CreateNumberComponentView numCompView = (CreateNumberComponentView) componentView;
        if (numCompView.getIdx() == requestCode) {
          numCompView.getNumberEditText().setText(value.toPlainString());
        }
      }
    }
  }

  List<GetLocationTask.GetLocationTaskCallback> getLocationCallbacks = new ArrayList<>();

  public void getLocation(final GetLocationTask.GetLocationTaskCallback callback) {
    if (ActivityCompat.checkSelfPermission(this,
      Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      new GetLocationTask(callback).execute(this);
    } else {
      ActivityCompat.requestPermissions(this,
        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
      getLocationCallbacks.add(callback);
    }
  }

  @Override
  public void onRequestPermissionsResult(
    final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (getLocationCallbacks.size() > 0 && requestCode == 1 &&
      grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
      new GetLocationTask(
        getLocationCallbacks.toArray(new GetLocationTask.GetLocationTaskCallback[0])).execute(this);
      getLocationCallbacks.clear();
    }
  }
}
