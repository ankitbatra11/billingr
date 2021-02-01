package com.abatra.billingr.google;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.PurchaseFetcher;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuDetailsFetcher;
import com.abatra.billingr.SkuPurchaser;
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

import bolts.Task;
import timber.log.Timber;

public class GoogleBillingr implements Billingr {

    private static final String LOG_TAG = "GoogleBillingClient";

    private final Context context;
    private final InitializedBillingClientSupplier billingClientSupplier;
    private final PurchaseFetcher purchaseFetcher;
    private final SkuDetailsFetcher skuDetailsFetcher;
    private final SkuPurchaser skuPurchaser;
    private BillingClient billingClient;
    private GooglePurchasesUpdatedListener purchasesUpdatedListener;

    @Nullable
    private GoogleBillingClientStateListener clientStateListener;

    public GoogleBillingr(Context context,
                          InitializedBillingClientSupplier billingClientSupplier,
                          PurchaseFetcher purchaseFetcher,
                          SkuDetailsFetcher skuDetailsFetcher,
                          SkuPurchaser skuPurchaser) {
        this.context = context;
        this.billingClientSupplier = billingClientSupplier;
        this.purchaseFetcher = purchaseFetcher;
        this.skuDetailsFetcher = skuDetailsFetcher;
        this.skuPurchaser = skuPurchaser;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void loadBilling(LoadBillingRequest loadBillingRequest) {

        purchasesUpdatedListener = new GooglePurchasesUpdatedListener(loadBillingRequest);

        billingClient = createBillingClient(loadBillingRequest, purchasesUpdatedListener);
        purchasesUpdatedListener.setBillingClient(billingClient);

        if (!billingClient.isReady()) {
            clientStateListener = new GoogleBillingClientStateListener(loadBillingRequest);
            billingClient.startConnection(clientStateListener);
        }
    }

    private BillingClient createBillingClient(LoadBillingRequest loadBillingRequest,
                                              GooglePurchasesUpdatedListener purchasesUpdatedListener) {
        BillingClient.Builder builder = BillingClient.newBuilder(context);
        if (loadBillingRequest.isEnablePendingPurchases()) {
            builder.enablePendingPurchases();
        }
        builder.setListener(purchasesUpdatedListener);
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

        Timber.tag(LOG_TAG).v("Querying purchases with request=%s", queryPurchasesRequest);

        String googleSkuType = GoogleBillingUtils.getSkuType(queryPurchasesRequest.getSkuType());
        Task.callInBackground(() -> billingClient.queryPurchases(googleSkuType)).continueWith(task -> {
            if (task.getError() != null) {
                Timber.e(task.getError(), "queryPurchases failed!");
                purchasesUpdatedListener.onLoadingPurchasesFailed(new LoadingPurchasesFailedException(task.getError()));
            } else {
                Timber.v("purchases result=%s", task.getResult());
                purchasesUpdatedListener.onPurchasesResultReceived(task.getResult());
            }
            return null;
        });
    }

    @Override
    public boolean launchPurchaseFlow(Activity activity, Sku sku) {

        GoogleSku googleSku = (GoogleSku) sku;
        BillingResult billingResult = billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                .setSkuDetails(googleSku.getSkuDetails())
                .build());

        return GoogleBillingResult.wrap(billingResult).isOk();
    }

    @Override
    public void destroy() {

        purchasesUpdatedListener = null;

        if (clientStateListener != null) {
            clientStateListener.destroy();
            clientStateListener = null;
        }

        if (billingClient != null) {
            if (billingClient.isReady()) {
                billingClient.endConnection();
            }
            billingClient = null;
        }
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
        purchaseFetcher.fetchInAppPurchases(listener);
    }

    @Override
    public void fetchInAppSkuDetails(List<String> skus, SkuDetailsFetcher.Listener listener) {
        skuDetailsFetcher.fetchInAppSkuDetails(skus, listener);
    }

    @Override
    public void addObserver(PurchaseListener observer) {
        skuPurchaser.addObserver(observer);
    }

    @Override
    public void removeObserver(PurchaseListener observer) {
        skuPurchaser.removeObserver(observer);
    }

    @Override
    public void forEachObserver(Consumer<PurchaseListener> observerConsumer) {
        skuPurchaser.forEachObserver(observerConsumer);
    }

    @Override
    public void removeObservers() {
        skuPurchaser.removeObservers();
    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, SkuPurchaser.Listener listener) {
        skuPurchaser.launchPurchaseFlow(sku, activity, listener);
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
            Timber.d("querySkuDetailsAsync result=%s", result);

            SkuListener skuListener = querySkuRequest.getSkuListener();
            if (skuListener != null) {
                if (result.isOk()) {
                    List<Sku> skus = new ArrayList<>();
                    for (SkuDetails skuDetails : list == null ? Collections.<SkuDetails>emptyList() : list) {
                        skus.add(new GoogleSku(skuType, skuDetails));
                    }
                    Timber.d("skusLoaded=%s", skus);
                    skuListener.onSkuLoaded(skus);
                } else {
                    String message = billingResult.getDebugMessage();
                    skuListener.onLoadingSkusFailed(new LoadingSkuFailedException(message));
                    Timber.w("loading skus failed");
                }
            } else {
                Timber.i("skuListener is not present");
            }
        }
    }

    /**
     * Called when the {@link BillingClient} is loaded.
     */
    private class GoogleBillingClientStateListener implements BillingClientStateListener {

        @Nullable
        private LoadBillingRequest loadBillingRequest;

        private GoogleBillingClientStateListener(@Nullable LoadBillingRequest loadBillingRequest) {
            this.loadBillingRequest = loadBillingRequest;
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

            GoogleBillingResult result = GoogleBillingResult.wrap(billingResult);
            Timber.d("startConnection result=%s", result);

            if (loadBillingRequest != null && loadBillingRequest.getLoadBillingListener() != null) {
                GoogleLoadBillingResult loadBillingResult = new GoogleLoadBillingResult(result);
                loadBillingRequest.getLoadBillingListener().onLoadBillingResultReceived(loadBillingResult);
            }
            if (result.isOk()) {
                if (loadBillingRequest != null && loadBillingRequest.getQueryPurchasesRequest() != null) {
                    queryPurchases(loadBillingRequest.getQueryPurchasesRequest());
                }
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            Timber.d("onBillingServiceDisconnected");
        }

        void destroy() {
            loadBillingRequest = null;
        }
    }
}
