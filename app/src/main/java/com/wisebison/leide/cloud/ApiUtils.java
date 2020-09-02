package com.wisebison.leide.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Objects;

public class ApiUtils {
  private static final String TAG = "ApiUtils";

  /**
   * Acquire an access token for the API. First checks if there is a cached token in the
   * SharedPreferences that has not expired. If not, request a new token.
   *
   * @param context   for getting SharedPreferences
   * @param callback  to pass token back to on success
   */
  public static void getAccessToken(final Context context, final GetAccessTokenCallback callback) {
    // Check for an existing token in the shared preferences. If one is found with more than a
    // minutes before it expires, return that one.
    final String accessTokenPrefKey = "access_token";
    final String tokenExpiryPrefKey = "token_expiry";
    final SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    final String currentToken = prefs.getString(accessTokenPrefKey, null);
    if (currentToken != null) {
      final long currentExpiry = prefs.getLong(tokenExpiryPrefKey, -1);
      final long expiresInSeconds = (currentExpiry - System.currentTimeMillis()) / 1000;
      if (expiresInSeconds > 60) {
        callback.resolve(currentToken);
        return;
      }
    }

    // Request a new token via the getAccessToken Firebase function
    final FirebaseFunctions firebaseFunctions = FirebaseFunctions.getInstance();
    firebaseFunctions
      .getHttpsCallable("getAccessToken")
      .call()
      .continueWith(task -> {
        // This continuation runs on either success or failure, but if the task
        // has failed then getResult() will throw an Exception which will be
        // propagated down.
        return (HashMap) Objects.requireNonNull(task.getResult()).getData();
      }).addOnSuccessListener(results -> {
        final String token = (String) results.get("token");
        final long expiry = (long) results.get("expiry");
        callback.resolve(token);
        // Cache token for future requests
        prefs.edit().putString(accessTokenPrefKey, token).apply();
        prefs.edit().putLong(tokenExpiryPrefKey, expiry).apply();
      }).addOnFailureListener(e -> Log.e(TAG, "failed to load access token", e))
      .addOnCanceledListener(() -> Log.d(TAG, "access token load canceled"));
  }

  public interface GetAccessTokenCallback {
    void resolve(String token);
  }
}
