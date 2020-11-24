package com.wisebison.leide.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.wisebison.leide.util.AbstractCallback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class BillingUtil {

  private static final String TAG = "BillingUtil";

  private final Context context;

  private final BillingClient billingClient;

  private final List<String> allSkus;
  private final List<String> purchasedSkus;
  private final Map<String, SkuDetails> skuToDetails;

  private boolean userIsWhitelisted = false;

  private OnPurchasesUpdatedListener onPurchasesUpdatedListener;

  @Inject
  public BillingUtil(@ApplicationContext final Context context) {
    this.context = context;
    allSkus = Collections.singletonList("pro_3_month");
    purchasedSkus = new ArrayList<>();
    skuToDetails = new HashMap<>();
    // Set up the billingClient to handle new purchases
    billingClient = BillingClient.newBuilder(context).enablePendingPurchases()
      .setListener((billingResult, purchases) -> {
        // Handle new purchases
        final int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
          for (final Purchase purchase : purchases) {
            handlePurchase(purchase);
            purchasedSkus.add(purchase.getSku());
          }
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
          Log.d(TAG, "item already owned");
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
          Log.d(TAG, "purchase canceled");
        } else if (purchases == null) {
          Log.d(TAG, "purchases is null");
        }
      }).build();
  }

  /**
   * Check if the logged in user is subscribed to premium or is on the whitelist.
   *
   * @param callback to inform caller whether to grant permission for premium features
   */
  public void hasPremium(final AbstractCallback<Boolean> callback) {
    if (checkPremiumCache()) {
      callback.resolve(true);
      return;
    }
    checkWhiteList(isWhitelisted -> {
      if (isWhitelisted) {
        // Logged in user is on whitelist - bypass check for premium subscription
        userIsWhitelisted = true;
        cacheHasPremium();
        callback.resolve(true);
      } else {
        // Logged in user is not on whitelist - use billingClient load the SkuDetails for the
        // available products and add them to skuToDetails
        billingClient.startConnection(new BillingClientStateListener() {
          @Override
          public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
              final SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setSkusList(allSkus)
                .setType(BillingClient.SkuType.SUBS)
                .build();
              billingClient.querySkuDetailsAsync(params, (billingResult1, list) -> {
                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK &&
                  list != null) {
                  for (final SkuDetails skuDetails : list) {
                    skuToDetails.put(skuDetails.getSku(), skuDetails);
                  }
                  final boolean hasPremium = hasPremiumInternal();
                  if (hasPremium) {
                    cacheHasPremium();
                  }
                  callback.resolve(hasPremium);
                }
              });
            } else {
              Log.d(TAG, billingResult.getDebugMessage());
            }
          }

          @Override
          public void onBillingServiceDisconnected() {
            Log.d(TAG, "Billing service disconnected");
          }
        });
      }
    });
  }

  private boolean checkPremiumCache() {
    final SharedPreferences prefs =
      context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    final boolean hasPremiumCachedValue = prefs.getBoolean("has_premium", false);
    if (!hasPremiumCachedValue) {
      return false;
    }
    final long expiryMillis = prefs.getLong("has_premium_expiry", -1);
    if (expiryMillis == -1) {
      return false;
    }
    final DateTime expiry = new DateTime(expiryMillis);
    return expiry.isAfterNow();
  }

  private void cacheHasPremium() {
    final SharedPreferences prefs =
      context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    final long expiry = DateTime.now().plus(DateTimeConstants.MILLIS_PER_HOUR).getMillis();
    prefs.edit()
      .putBoolean("has_premium", true)
      .putLong("has_premium_expiry", expiry)
      .apply();
  }

  private void checkWhiteList(final AbstractCallback<Boolean> callback) {
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("must be logged in");
    }
    final DatabaseReference whitelistRef =
      FirebaseDatabase.getInstance().getReference("premium-whitelist");
    whitelistRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull final DataSnapshot snapshot) {
        final List<String> whiteList =
          snapshot.getValue(new GenericTypeIndicator<List<String>>() {});
        callback.resolve(Objects.requireNonNull(whiteList).contains(currentUser.getEmail()));
      }

      @Override
      public void onCancelled(@NonNull final DatabaseError error) { }
    });
  }

  private void handlePurchase(final Purchase purchase) {
    if (!purchase.isAcknowledged()) {
      final AcknowledgePurchaseParams acknowledgePurchaseParams =
        AcknowledgePurchaseParams.newBuilder()
          .setPurchaseToken(purchase.getPurchaseToken())
          .build();
      billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
          onPurchasesUpdatedListener.onPurchasesUpdated();
          Log.d(TAG, "purchase acknowledged");
        }
      });
    } else {
      Log.d(TAG, "purchase already acknowledged");
    }
  }

  private boolean hasPremiumInternal() {
    purchasedSkus.clear();
    final Purchase.PurchasesResult purchasesResult =
      billingClient.queryPurchases(BillingClient.SkuType.SUBS);
    final List<Purchase> purchasesList = purchasesResult.getPurchasesList();
    if (purchasesList != null) {
      for (final Purchase purchase : purchasesList) {
        if (allSkus.contains(purchase.getSku())) {
          purchasedSkus.add(purchase.getSku());
        }
      }
    }
    if (!allSkus.contains("pro_3_month")) {
      throw new IllegalArgumentException();
    }
    return purchasedSkus.contains("pro_3_month");
  }

  public String getPrice(final String sku) {
    final SkuDetails skuDetails = skuToDetails.get(sku);
    if (skuDetails == null) {
      throw new IllegalArgumentException("SKU " + sku + " not found");
    }
    return skuDetails.getPrice();
  }

  public void purchase(
    final String sku, final Activity activity,
    final OnPurchasesUpdatedListener onPurchasesUpdatedListener) {
    if (userIsWhitelisted) {
      Log.e(TAG, "purchase called when user is whitelisted");
      return;
    }
    this.onPurchasesUpdatedListener = onPurchasesUpdatedListener;
    final SkuDetails skuDetails = skuToDetails.get(sku);
    if (skuDetails == null) {
      throw new IllegalArgumentException("SKU " + sku + " not found");
    }
    final BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
      .setSkuDetails(skuDetails)
      .build();
    billingClient.launchBillingFlow(activity, billingFlowParams);
  }

  /**
   * Listener to inform caller that a new purchase has been made by the user.
   */
  public interface OnPurchasesUpdatedListener {
    void onPurchasesUpdated();
  }
}
