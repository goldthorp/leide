package com.wisebison.leide.view;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.location.LocationServices;
import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.DiaryEntryDao;
import com.wisebison.leide.model.DiaryEntry;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateDiaryEntryActivity extends AppCompatActivity {

  private long startTimestamp;

  /**
   * Dialog to display on save click for setting location text and selecting timezone.
   * Location will be autofilled with the device's last location if available.
   * Timezone will be set to the device's timezone.
   */
  private AlertDialog locationDialog;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_diary_entry);

    startTimestamp = System.currentTimeMillis();

    setUpLocationDialog();
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

  private void setUpLocationDialog() {
    final LayoutInflater inflater = getLayoutInflater();
    final LinearLayout dialogLayout = new LinearLayout(this);
    inflater.inflate(R.layout.dialog_location_timezone, dialogLayout);

    // Spinner to select a timezone
    final Spinner timeZoneSpinner = dialogLayout.findViewById(R.id.timezone_spinner);
    final ArrayAdapter<String> timeZoneIdAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            TimeZone.getAvailableIDs());
    timeZoneIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    timeZoneSpinner.setAdapter(timeZoneIdAdapter);

    // Set selected item in spinner to current timezone
    for (int i = 0; i < timeZoneIdAdapter.getCount(); i++) {
      if (TimeZone.getDefault().getID().equals(timeZoneIdAdapter.getItem(i))) {
        timeZoneSpinner.setSelection(i);
      }
    }

    // EditText for location
    final EditText locationEditText = dialogLayout.findViewById(R.id.location_edit_text);

    // Spinner for choosing address to auto-fill in the EditText
    final Spinner locationSpinner = dialogLayout.findViewById(R.id.location_spinner);
    final ArrayAdapter<String> addressAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

    // Get the device's last known location and reverse geocode the coordinates on a background
    // thread. Populate the spinner with the addresses for auto-filling the location input.
    new GetLocationTask(addresses -> {
      for (final Address address : addresses) {
        addressAdapter.add(address.getAddressLine(0));
      }
      locationSpinner.setAdapter(addressAdapter);
    }).execute(this);

    // When a spinner item is selected, set the EditText value
    locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(
          final AdapterView<?> parent, final View view, final int position, final long id) {
        locationEditText.setText(addressAdapter.getItem(position));
      }
      @Override
      public void onNothingSelected(final AdapterView<?> parent) {}
    });

    // Spinner for choosing from recently used locations
    final Spinner recentLocationsSpinner = dialogLayout.findViewById(R.id.recent_locations_spinner);
    final ArrayAdapter<String> recentAddressesAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
    // Add a placeholder to the recent locations spinner
    recentAddressesAdapter.add("-");

    // Get recent locations
    final DiaryEntryDao entryDao = AppDatabase.getInstance(this).getDiaryEntryDao();
    final LiveData<List<String>> recentLocationsLiveData = entryDao.getRecentLocations();
    recentLocationsLiveData.observe(this, new Observer<List<String>>() {
      @Override
      public void onChanged(final List<String> strings) {
        // Remove observer immediately since this is a one-time thing
        recentLocationsLiveData.removeObserver(this);

        // Populate spinner with the 5 most recent addresses
        recentAddressesAdapter.addAll(strings);
        recentLocationsSpinner.setAdapter(recentAddressesAdapter);
      }
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
      public void onNothingSelected(final AdapterView<?> parent) {}
    });

    // Build dialog
    locationDialog = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.set_location))
        .setView(dialogLayout)
        .setPositiveButton(R.string.save, (dialog, which) -> {
          saveEntry(locationEditText.getText().toString(),
              (String) timeZoneSpinner.getSelectedItem());
        }).create();
  }

  public void onClickSave(final View view) {
    locationDialog.show();
  }

  private void saveEntry(final String location, final String timeZone) {
    final EditText editText = findViewById(R.id.editText);
    final DiaryEntry entry = new DiaryEntry();
    entry.setText(editText.getText().toString());
    entry.setStartTimestamp(startTimestamp);
    entry.setSaveTimestamp(System.currentTimeMillis());
    entry.setLocation(location);
    entry.setTimeZone(timeZone);

    // Insert on a separate thread because otherwise room throws an error
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final DiaryEntryDao entryDao = AppDatabase.getInstance(this).getDiaryEntryDao();
    try {
      executorService.submit(() -> entryDao.insert(entry)).get();
      finish();
    } catch (final ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Task to get the last known location for the device and reverse geocode the coordinates (get
   * a human-readable address from the latitude/longitude of the device).
   *
   * This is used to pre-populate the location EditText in the set location dialog that displays
   * on save.
   */
  private static class GetLocationTask extends AsyncTask<Context, Void, Void> {

    private final GetLocationTaskCallback callback;
    GetLocationTask(final GetLocationTaskCallback callback) {
      this.callback = callback;
    }

    @Override
    protected Void doInBackground(final Context... contexts) {
      // TODO request permission
      LocationServices.getFusedLocationProviderClient(contexts[0]).getLastLocation()
          .addOnSuccessListener(location -> {
            if (location != null) {
              // Device's last location found. Attempt to reverse geocode the coordinates.
              final Geocoder geocoder = new Geocoder(contexts[0], Locale.getDefault());
              try {
                final List<Address> address = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 5);
                if (!CollectionUtils.isEmpty(address) &&
                    StringUtils.isNotBlank(address.get(0).getAddressLine(0))) {
                  callback.resolve(address);
                } else {
                  Log.d("DEBUG", "address not found");
                }
              } catch (final IOException e) {
                e.printStackTrace();
              }
            } else {
              Log.d("DEBUG", "location not found");
            }
          }).addOnFailureListener(e -> {
            Log.e("GET_LOCATION_FAILED", e.getMessage(), e);
          });
      return null;
    }
  }

  private interface GetLocationTaskCallback {
    void resolve(List<Address> addresses);
  }
}
