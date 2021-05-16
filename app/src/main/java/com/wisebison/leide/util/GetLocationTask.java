package com.wisebison.leide.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Task to get the last known location for the device and reverse geocode the coordinates (get
 * a human-readable address from the latitude/longitude of the device).
 */
public class GetLocationTask extends AsyncTask<Context, Void, Void> {

  private final GetLocationTaskCallback[] callbacks;
  public GetLocationTask(final GetLocationTaskCallback... callback) {
    this.callbacks = callback;
  }

  @Override
  protected Void doInBackground(final Context... contexts) {
    if (ActivityCompat.checkSelfPermission(contexts[0],
      Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                for (final GetLocationTaskCallback callback : callbacks) {
                  callback.resolve(address);
                }
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
    } else {
      Log.e(GetLocationTask.class.getSimpleName(), Manifest.permission.ACCESS_FINE_LOCATION +
        " permission missing. This should be requested from the user before executing a" +
        " GetLocationTask");
    }

    return null;
  }

  public interface GetLocationTaskCallback {
    void resolve(List<Address> addresses);
  }
}