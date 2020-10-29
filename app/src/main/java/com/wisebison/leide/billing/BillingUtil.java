package com.wisebison.leide.billing;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Setter;

public class BillingUtil implements PurchasesUpdatedListener {

  private static final String TAG = "BillingUtil";

  private final BillingClient billingClient;

  private final List<String> allSkus;
  private final List<String> purchasedSkus;
  private final Map<String, SkuDetails> skuToDetails;

  private boolean userIsWhitelisted = false;

  private OnPurchasesUpdatedListener onPurchasesUpdatedListener;

  @Setter
  private OnBillingUtilReadyListener onBillingUtilReadyListener;

  private final Activity activity;

  public BillingUtil(final Activity activity) {
    this.activity = activity;
    allSkus = Collections.singletonList("pro_3_month");
    purchasedSkus = new ArrayList<>();
    billingClient =
      BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build();
    skuToDetails = new HashMap<>();

    checkWhiteList(isWhitelisted -> {
      if (isWhitelisted) {
        // Logged in user is on whitelist - bypass check for premium subscription
        userIsWhitelisted = true;
        onBillingUtilReadyListener.onBillingUtilReady();
      } else {
        billingClient.startConnection(new BillingClientStateListener() {
          @Override
          public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
              loadSkus();
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

  private void checkWhiteList(final CheckWhiteListCallback callback) {
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

  private void loadSkus() {
    if (!billingClient.isReady()) {
      throw new IllegalStateException("BillingClient is not ready");
    }
    final SkuDetailsParams params = SkuDetailsParams.newBuilder()
      .setSkusList(allSkus)
      .setType(BillingClient.SkuType.SUBS)
      .build();
    billingClient.querySkuDetailsAsync(params, (billingResult, list) -> {
      if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
        for (final SkuDetails skuDetails : list) {
          skuToDetails.put(skuDetails.getSku(), skuDetails);
        }
        if (onBillingUtilReadyListener != null) {
          onBillingUtilReadyListener.onBillingUtilReady();
        }
      }
    });
  }

  private void loadPurchases() {
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
  }

  @Override
  public void onPurchasesUpdated(
    @NonNull final BillingResult billingResult, @Nullable final List<Purchase> purchases) {
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
    }
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

  private boolean isPurchased(final String sku) {
    if (!allSkus.contains(sku)) {
      throw new IllegalArgumentException();
    }
    return purchasedSkus.contains(sku);
  }

  public boolean hasPremium() {
    if (userIsWhitelisted) {
      return true;
    }
    loadPurchases();
    return isPurchased("pro_3_month");
  }

  public String getPrice(final String sku) {
    final SkuDetails skuDetails = skuToDetails.get(sku);
    if (skuDetails == null) {
      throw new IllegalArgumentException("SKU " + sku + " not found");
    }
    return skuDetails.getPrice();
  }

  public void purchase(
    final String sku, final OnPurchasesUpdatedListener onPurchasesUpdatedListener) {
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

  public interface OnPurchasesUpdatedListener {
    void onPurchasesUpdated();
  }

  public interface OnBillingUtilReadyListener {
    void onBillingUtilReady();
  }

  private interface CheckWhiteListCallback {
    void resolve(boolean isWhitelisted);
  }
}
