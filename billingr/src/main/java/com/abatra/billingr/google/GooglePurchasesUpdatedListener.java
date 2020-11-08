package com.abatra.billingr.google;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.billingr.exception.LoadingPurchasesFailedException;
import com.abatra.billingr.load.LoadBillingRequest;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.ArrayList;
import java.util.List;

class GooglePurchasesUpdatedListener implements PurchasesUpdatedListener {

    private static final String LOG_TAG = "GooglePurchasesUpdated";

    private final LoadBillingRequest loadBillingRequest;
    private BillingClient billingClient;

    GooglePurchasesUpdatedListener(LoadBillingRequest loadBillingRequest) {
        this.loadBillingRequest = loadBillingRequest;
    }

    void setBillingClient(BillingClient billingClient) {
        this.billingClient = billingClient;
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
        Log.d(LOG_TAG, "queryPurchases result=" + result);

        if (result.isOk()) {
            acknowledgeAndNotifyPurchases(list);
        }
    }

    void onPurchasesResultReceived(Purchase.PurchasesResult purchasesResult) {
        if (purchasesResult != null) {
            acknowledgeAndNotifyPurchases(purchasesResult.getPurchasesList());
        }
    }

    private void acknowledgeAndNotifyPurchases(List<Purchase> purchases) {
        List<com.abatra.billingr.purchase.Purchase> result = new ArrayList<>();
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    acknowledge(purchase);
                    result.add(GoogleBillingUtils.toPurchase(purchase));
                }
            }
        }
        if (loadBillingRequest.getPurchaseListener() != null) {
            loadBillingRequest.getPurchaseListener().onPurchasesUpdated(result);
        }
    }

    private void acknowledge(Purchase purchase) {

        if (!purchase.isAcknowledged()) {

            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
                Log.d(LOG_TAG, "orderId=" + purchase.getOrderId() + " acknowledgePurchase result=" + result);
            });
        }
    }

    public void onLoadingPurchasesFailed(LoadingPurchasesFailedException e) {
        if (loadBillingRequest.getPurchaseListener() != null) {
            loadBillingRequest.getPurchaseListener().onLoadingPurchasesFailed(e);
        }
    }
}
