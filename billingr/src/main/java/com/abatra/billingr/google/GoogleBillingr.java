package com.abatra.billingr.google;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.billingr.Billingr;
import com.abatra.billingr.exception.LoadingPurchasesFailedException;
import com.abatra.billingr.exception.LoadingSkuFailedException;
import com.abatra.billingr.load.LoadBillingRequest;
import com.abatra.billingr.purchase.QueryPurchasesRequest;
import com.abatra.billingr.sku.QuerySkuRequest;
import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuListener;
import com.abatra.billingr.sku.SkuType;
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
import java.util.Timer;

import bolts.Task;

public class GoogleBillingr implements Billingr {

    private static final String LOG_TAG = "GoogleBillingClient";

    private final Context context;
    private BillingClient billingClient;
    private GooglePurchasesUpdatedListener googlePurchasesUpdatedListener;

    public GoogleBillingr(Context context) {
        this.context = context;
    }

    @Override
    public void loadBilling(LoadBillingRequest loadBillingRequest) {

        googlePurchasesUpdatedListener = new GooglePurchasesUpdatedListener(loadBillingRequest);

        billingClient = createBillingClient(loadBillingRequest);
        googlePurchasesUpdatedListener.setBillingClient(billingClient);

        if (!billingClient.isReady()) {
            billingClient.startConnection(new GoogleBillingClientStateListener(loadBillingRequest));
        }
    }

    private BillingClient createBillingClient(LoadBillingRequest loadBillingRequest) {
        BillingClient.Builder builder = BillingClient.newBuilder(context);
        if (loadBillingRequest.isEnablePendingPurchases()) {
            builder.enablePendingPurchases();
        }
        builder.setListener(googlePurchasesUpdatedListener);
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

        Log.v(LOG_TAG, "Querying purchases with request=" + queryPurchasesRequest);

        String googleSkuType = GoogleBillingUtils.getSkuType(queryPurchasesRequest.getSkuType());
        Task.callInBackground(() -> billingClient.queryPurchases(googleSkuType)).continueWith(task -> {
            if (task.getError() != null) {
                Log.e(LOG_TAG, "queryPurchases failed!", task.getError());
                googlePurchasesUpdatedListener.onLoadingPurchasesFailed(new LoadingPurchasesFailedException(task.getError()));
            } else {
                Log.v(LOG_TAG, "purchases result=" + task.getResult());
                googlePurchasesUpdatedListener.onPurchasesResultReceived(task.getResult());
            }
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

        googlePurchasesUpdatedListener = null;

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

            SkuListener skuListener = querySkuRequest.getSkuListener();
            if (skuListener != null) {
                if (result.isOk()) {
                    List<Sku> skus = new ArrayList<>();
                    for (SkuDetails skuDetails : list == null ? Collections.<SkuDetails>emptyList() : list) {
                        skus.add(new GoogleSku(skuType, skuDetails));
                    }
                    Log.d(LOG_TAG, "skusLoaded=" + skus);
                    skuListener.onSkuLoaded(skus);
                } else {
                    String message = billingResult.getDebugMessage();
                    skuListener.onLoadingSkusFailed(new LoadingSkuFailedException(message));
                    Log.w(LOG_TAG, "loading skus failed");
                }
            } else {
                Log.i(LOG_TAG, "skuListener is not present");
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
