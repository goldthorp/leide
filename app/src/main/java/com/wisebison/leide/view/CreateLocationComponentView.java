package com.wisebison.leide.view;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryComponentDao;
import com.wisebison.leide.model.EntryComponent;
import com.wisebison.leide.model.EntryComponentType;
import com.wisebison.leide.model.EntryComponentValue;

import org.apache.commons.lang3.StringUtils;

public class CreateLocationComponentView extends ComponentView {

  private String locationDisplay;

  private final TextView locationTextView;
  private final ProgressBar progressBar;

  private AlertDialog editDialog;

  public CreateLocationComponentView(final Context context) {
    this(context, null);
  }

  public CreateLocationComponentView(final Context context, final String name) {
    super(context, name);

    inflate(context, R.layout.view_create_location_component, this);
    locationTextView = findViewById(R.id.location_text_view);
    progressBar = findViewById(R.id.progress_bar);
    setUpLocationDialog();

    final ImageView editLocation = findViewById(R.id.edit_location);
    editLocation.setOnClickListener(v -> editDialog.show());
  }

  private void setUpLocationDialog() {
    final LayoutInflater inflater = LayoutInflater.from(getContext());
    final LinearLayout dialogLayout = new LinearLayout(getContext());
    inflater.inflate(R.layout.dialog_location, dialogLayout);

    // EditText for location
    final EditText locationEditText = dialogLayout.findViewById(R.id.location_edit_text);

    // Spinner for choosing address to auto-fill in the EditText
    final Spinner locationSpinner = dialogLayout.findViewById(R.id.location_spinner);
    final ArrayAdapter<String> addressAdapter =
      new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);

    // Get the device's last known location and reverse geocode the coordinates on a background
    // thread. Populate the spinner with the addresses for auto-filling the location input.
    if (getContext() instanceof CreateEntryActivity) {
      ((CreateEntryActivity) (getContext())).getLocation(addresses -> {
        for (final Address address : addresses) {
          addressAdapter.add(address.getAddressLine(0));
        }
        locationSpinner.setAdapter(addressAdapter);
        setLocationDisplay(addresses.get(0).getAddressLine(0));
      });
    }

    // When a spinner item is selected, set the EditText value
    locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(
        final AdapterView<?> parent, final View view, final int position, final long id) {
        locationEditText.setText(addressAdapter.getItem(position));
      }

      @Override
      public void onNothingSelected(final AdapterView<?> parent) {
      }
    });

    // Spinner for choosing from recently used locations
    final Spinner recentLocationsSpinner = dialogLayout.findViewById(R.id.recent_locations_spinner);
    final ArrayAdapter<String> recentAddressesAdapter =
      new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
    // Add a placeholder to the recent locations spinner
    recentAddressesAdapter.add("-");

    // Get recent locations
    final EntryComponentDao entryComponentDao =
      AppDatabase.getInstance(getContext()).getEntryComponentDao();
    entryComponentDao.getRecentLocations().then(locationJsonStrings -> {
      // Populate spinner with the 5 most recent addresses
      for (final String location : locationJsonStrings) {
        recentAddressesAdapter.add(location);
      }
      recentLocationsSpinner.setAdapter(recentAddressesAdapter);
    });

    // When a recent location is selected, set the EditText value
    recentLocationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(
        final AdapterView<?> parent, final View view, final int position, final long id) {
        if (position != 0) {
          locationEditText.setText(recentAddressesAdapter.getItem(position));
        }
      }

      @Override
      public void onNothingSelected(final AdapterView<?> parent) {
      }
    });

    // Build dialog
    editDialog = new AlertDialog.Builder(getContext())
      .setTitle(getResources().getString(R.string.set_location))
      .setView(dialogLayout)
      .setPositiveButton(R.string.save, (dialog, which) -> {
        // TODO: validate that a value is specified
        setLocationDisplay(locationEditText.getText().toString());
      }).create();
  }

  public void setLocationDisplay(final String locationDisplay) {
    this.locationDisplay = locationDisplay;
    locationTextView.setText(locationDisplay);
    if (StringUtils.isNotBlank(locationDisplay)) {
      locationTextView.setVisibility(VISIBLE);
      progressBar.setVisibility(GONE);
    }
  }

  @Override
  public EntryComponent getComponent() {
    final EntryComponent component = new EntryComponent(EntryComponentType.LOCATION);
    component.getValues().add(new EntryComponentValue("display", locationDisplay));
    return component;
  }
}
