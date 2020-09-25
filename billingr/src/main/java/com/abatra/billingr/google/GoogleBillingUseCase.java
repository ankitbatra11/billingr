package com.abatra.billingr.google;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.billingr.BillingUseCase;
import com.abatra.billingr.LoadBillingRequest;
import com.abatra.billingr.QueryPurchasesRequest;
import com.abatra.billingr.QuerySkuRequest;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuType;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
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
    private BillingClient billingClient;
    private GoogleBillingPurchaseUpdatedListener purchaseUpdatedListener;

    public GoogleBillingUseCase(Context context) {
        this.context = context;
    }

    @Override
    public void loadBilling(LoadBillingRequest loadBillingRequest) {

        purchaseUpdatedListener = new GoogleBillingPurchaseUpdatedListener();

        billingClient = createBillingClient(loadBillingRequest);
        purchaseUpdatedListener.setBillingClient(billingClient);

        if (!billingClient.isReady()) {
            billingClient.startConnection(new GoogleBillingClientStateListener(loadBillingRequest, billingClient));
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
                new GooglePurchaseHistoryResponseListener(queryPurchasesRequest));
    }

    @Override
    public void acknowledgePurchases(SkuType skuType) {
        billingClient.queryPurchases(GoogleBillingUtils.getSkuType(skuType));
    }

    @Override
    public void destroy() {

        purchaseUpdatedListener = null;

        if (billingClient != null) {
            if (billingClient.isReady()) {
                billingClient.endConnection();
            }
            billingClient = null;
        }
    }

    private static class GooglePurchaseHistoryResponseListener implements PurchaseHistoryResponseListener {

        private final QueryPurchasesRequest queryPurchasesRequest;

        private GooglePurchaseHistoryResponseListener(QueryPurchasesRequest queryPurchasesRequest) {
            this.queryPurchasesRequest = queryPurchasesRequest;
        }

        @Override
        public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult,
                                              @Nullable List<PurchaseHistoryRecord> list) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Log.d(LOG_TAG, "queryPurchaseHistoryAsync result=" + result);

            if (queryPurchasesRequest.getPurchaseListener() != null && result.isOk() && list != null) {
                List<com.abatra.billingr.Purchase> purchases = new ArrayList<>();
                for (PurchaseHistoryRecord record : list) {
                    purchases.add(GoogleBillingUtils.toPurchase(record));
                }
                queryPurchasesRequest.getPurchaseListener().onPurchasesUpdated(purchases);
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
                    Sku sku = GoogleBillingUtils.toSku(skuDetails);
                    sku.setType(skuType);
                    skus.add(sku);
                }
                querySkuRequest.getSkuListener().onSkuLoaded(skus);
            }
        }
    }

    private static class GoogleBillingClientStateListener implements BillingClientStateListener {

        private final LoadBillingRequest loadBillingRequest;
        private final BillingClient billingClient;

        private GoogleBillingClientStateListener(LoadBillingRequest loadBillingRequest, BillingClient billingClient) {
            this.billingClient = billingClient;
            this.loadBillingRequest = loadBillingRequest;
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Log.d(LOG_TAG, "startConnection result=" + result);

            if (loadBillingRequest.getLoadBillingListener() != null) {
                GoogleLoadBillingResult availabilityResult = new GoogleLoadBillingResult(result);
                loadBillingRequest.getLoadBillingListener().onBillingAvailabilityStatusUpdated(availabilityResult);
            }

            if (!loadBillingRequest.getAcknowledgePurchasesSkuTypes().isEmpty()) {
                for (SkuType skuType : loadBillingRequest.getAcknowledgePurchasesSkuTypes()) {
                    billingClient.queryPurchases(GoogleBillingUtils.getSkuType(skuType));
                }
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            Log.d(LOG_TAG, "onBillingServiceDisconnected");
        }
    }

    private static class GoogleBillingPurchaseUpdatedListener implements PurchasesUpdatedListener {

        private BillingClient billingClient;

        public void setBillingClient(BillingClient billingClient) {
            this.billingClient = billingClient;
        }

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
