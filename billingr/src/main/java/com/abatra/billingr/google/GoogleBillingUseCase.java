package com.abatra.billingr.google;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.billingr.BillingUseCase;
import com.abatra.billingr.LoadBillingRequest;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.QueryPurchasesRequest;
import com.abatra.billingr.QuerySkuRequest;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuType;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GoogleBillingUseCase implements BillingUseCase {

    private static final String LOG_TAG = "GoogleBillingClient";

    private final Context context;
    private final GooglePurchaseHistoryResponseListener purchaseHistoryResponseListener;
    private final GoogleBillingPurchaseUpdatedListener purchaseUpdatedListener;

    private BillingClient billingClient;
    private PurchaseListener purchaseListener;

    public GoogleBillingUseCase(Context context) {
        this.context = context;
        purchaseHistoryResponseListener = new GooglePurchaseHistoryResponseListener();
        purchaseUpdatedListener = new GoogleBillingPurchaseUpdatedListener();
    }

    @Override
    public void loadBilling(LoadBillingRequest loadBillingRequest) {
        purchaseListener = loadBillingRequest.getPurchaseListener();
        billingClient = createBillingClient(loadBillingRequest);
        if (!billingClient.isReady()) {
            billingClient.startConnection(new GoogleBillingClientStateListener(loadBillingRequest));
        }
    }

    private BillingClient createBillingClient(LoadBillingRequest loadBillingRequest) {
        BillingClient.Builder builder = BillingClient.newBuilder(context);
        if (loadBillingRequest.isEnablePendingPurchases()) {
            builder.enablePendingPurchases();
        }
        builder.setListener(purchaseUpdatedListener);
        return builder.build();
    }

    @Override
    public void querySkus(QuerySkuRequest querySkuRequest) {
        for (Map.Entry<SkuType, Collection<String>> skuIdsByType : querySkuRequest.getSkuIdsByType().entrySet()) {

            SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                    .setType(GoogleBillingUtils.getSkuType(skuIdsByType.getKey()))
                    .setSkusList(new ArrayList<>(skuIdsByType.getValue()))
                    .build();

            billingClient.querySkuDetailsAsync(skuDetailsParams, new GoogleSkuDetailsResponseListener(skuIdsByType.getKey(), querySkuRequest));
        }
    }

    @Override
    public void queryPurchases(QueryPurchasesRequest queryPurchasesRequest) {
        billingClient.queryPurchaseHistoryAsync(GoogleBillingUtils.getSkuType(queryPurchasesRequest.getSkuType()),
                purchaseHistoryResponseListener);
    }

    @Override
    public void acknowledgePurchases(SkuType skuType) {
        billingClient.queryPurchases(GoogleBillingUtils.getSkuType(skuType));
    }

    @Override
    public void purchase(Activity activity, Sku sku) {
        GoogleSku googleSku = (GoogleSku) sku;
        billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                .setSkuDetails(googleSku.getSkuDetails())
                .build());
    }

    @Override
    public void destroy() {

        purchaseListener = null;

        if (billingClient != null) {
            if (billingClient.isReady()) {
                billingClient.endConnection();
            }
            billingClient = null;
        }
    }

    private class GooglePurchaseHistoryResponseListener implements PurchaseHistoryResponseListener {

        @Override
        public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult,
                                              @Nullable List<PurchaseHistoryRecord> list) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Log.d(LOG_TAG, "queryPurchaseHistoryAsync result=" + result);

            if (purchaseListener != null && result.isOk() && list != null) {
                List<com.abatra.billingr.Purchase> purchases = new ArrayList<>();
                for (PurchaseHistoryRecord record : list) {
                    purchases.add(GoogleBillingUtils.toPurchase(record));
                }
                purchaseListener.onPurchasesUpdated(purchases);
            }
        }
    }

    private static class GoogleSkuDetailsResponseListener implements SkuDetailsResponseListener {

        private final SkuType skuType;
        private final QuerySkuRequest querySkuRequest;

        private GoogleSkuDetailsResponseListener(SkuType skuType, QuerySkuRequest querySkuRequest) {
            this.skuType = skuType;
            this.querySkuRequest = querySkuRequest;
        }

        @Override
        public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Log.d(LOG_TAG, "querySkuDetailsAsync result=" + result);

            if (querySkuRequest.getSkuListener() != null && result.isOk()) {
                List<Sku> skus = new ArrayList<>();
                for (SkuDetails skuDetails : list == null ? Collections.<SkuDetails>emptyList() : list) {
                    skus.add(new GoogleSku(skuType, skuDetails));
                }
                querySkuRequest.getSkuListener().onSkuLoaded(skus);
            }
        }
    }

    private class GoogleBillingClientStateListener implements BillingClientStateListener {

        private final LoadBillingRequest loadBillingRequest;

        private GoogleBillingClientStateListener(LoadBillingRequest loadBillingRequest) {
            this.loadBillingRequest = loadBillingRequest;
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Log.d(LOG_TAG, "startConnection result=" + result);

            if (loadBillingRequest.getLoadBillingListener() != null) {
                GoogleLoadBillingResult loadBillingResult = new GoogleLoadBillingResult(result);
                loadBillingRequest.getLoadBillingListener().onLoadBillingResultReceived(loadBillingResult);
            }
            if (result.isOk()) {
                if (!loadBillingRequest.getAcknowledgePurchasesSkuTypes().isEmpty()) {
                    for (SkuType skuType : loadBillingRequest.getAcknowledgePurchasesSkuTypes()) {
                        acknowledgePurchases(skuType);
                    }
                }
                if (loadBillingRequest.getQueryPurchasesRequest() != null) {
                    queryPurchases(loadBillingRequest.getQueryPurchasesRequest());
                }
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            Log.d(LOG_TAG, "onBillingServiceDisconnected");
        }
    }

    private class GoogleBillingPurchaseUpdatedListener implements PurchasesUpdatedListener {

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                       @Nullable List<Purchase> list) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Log.d(LOG_TAG, "queryPurchases result=" + result);

            if (result.isOk() && list != null) {
                for (Purchase purchase : list) {
                    acknowledge(purchase);
                }
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
    }
}
