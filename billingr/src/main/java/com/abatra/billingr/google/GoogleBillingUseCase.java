package com.abatra.billingr.google;

import android.app.Activity;
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
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bolts.Task;

public class GoogleBillingUseCase implements BillingUseCase {

    private static final String LOG_TAG = "GoogleBillingClient";

    private final Context context;
    private BillingClient billingClient;
    private GooglePurchaseHandler googlePurchaseHandler;

    public GoogleBillingUseCase(Context context) {
        this.context = context;
    }

    @Override
    public void loadBilling(LoadBillingRequest loadBillingRequest) {

        googlePurchaseHandler = new GooglePurchaseHandler(loadBillingRequest.getPurchaseListener());

        billingClient = createBillingClient(loadBillingRequest);
        googlePurchaseHandler.setBillingClient(billingClient);

        if (!billingClient.isReady()) {
            billingClient.startConnection(new GoogleBillingClientStateListener(loadBillingRequest));
        }
    }

    private BillingClient createBillingClient(LoadBillingRequest loadBillingRequest) {
        BillingClient.Builder builder = BillingClient.newBuilder(context);
        if (loadBillingRequest.isEnablePendingPurchases()) {
            builder.enablePendingPurchases();
        }
        builder.setListener(googlePurchaseHandler);
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
        String googleSkuType = GoogleBillingUtils.getSkuType(queryPurchasesRequest.getSkuType());
        Task.callInBackground(() -> billingClient.queryPurchases(googleSkuType)).continueWith(task -> {
            if (task.getError() != null) {
                Log.e(LOG_TAG, "queryPurchases failed!", task.getError());
            }
            googlePurchaseHandler.onPurchasesResultReceived(task.getResult());
            return null;
        });
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

        googlePurchaseHandler = null;

        if (billingClient != null) {
            if (billingClient.isReady()) {
                billingClient.endConnection();
            }
            billingClient = null;
        }
    }

    /**
     * Called when Skus are loaded.
     */
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

    /**
     * Called when the {@link BillingClient} is loaded.
     */
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
}
