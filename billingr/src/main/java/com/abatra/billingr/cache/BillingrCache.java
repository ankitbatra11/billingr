package com.abatra.billingr.cache;

import android.app.Activity;
import android.content.SharedPreferences;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuDetailsFetcher;
import com.abatra.billingr.SkuPurchaser;
import com.abatra.billingr.exception.LoadingSkuFailedException;
import com.abatra.billingr.load.LoadBillingRequest;
import com.abatra.billingr.purchase.QueryPurchasesRequest;
import com.abatra.billingr.sku.QuerySkuRequest;
import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuListener;
import com.abatra.billingr.sku.SkuType;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import bolts.Task;
import timber.log.Timber;

public class BillingrCache implements Billingr {

    private final Billingr billingr;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public BillingrCache(Billingr billingr, SharedPreferences sharedPreferences, Gson gson) {
        this.billingr = billingr;
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
    }

    @Override
    public void loadBilling(LoadBillingRequest loadBillingRequest) {
        billingr.loadBilling(loadBillingRequest);
    }

    @Override
    public void querySkus(QuerySkuRequest querySkuRequest) {
        if (querySkuRequest.queryFromCache()) {
            querySkusFromCache(querySkuRequest, new LoadingSkuFailedException("Failed to load skus from cache!"));
        } else {
            billingr.querySkus(decorate(querySkuRequest));
        }
    }

    private QuerySkuRequest decorate(QuerySkuRequest querySkuRequest) {
        return QuerySkuRequest.builder()
                .forSku(querySkuRequest.getSkuIdsByType())
                .setSkuListener(new CacheSkuListener(querySkuRequest))
                .build();
    }

    private void cache(List<Sku> skus) {
        Task.callInBackground(() -> {
            for (Sku sku : skus) {
                try {
                    tryCaching(sku);
                } catch (Throwable t) {
                    Timber.e(t, "Failed to cache sku=%s", sku);
                }
            }
            return null;
        });
    }

    private void tryCaching(Sku sku) {
        String skuKey = createSkuKey(sku.getType(), sku.getId());
        String value = gson.toJson(sku);
        sharedPreferences.edit().putString(skuKey, value).apply();
    }

    private void querySkusFromCache(QuerySkuRequest querySkuRequest, LoadingSkuFailedException e) {
        Task.callInBackground(() ->
        {
            List<Sku> result = new ArrayList<>();
            for (Map.Entry<SkuType, Collection<String>> entry : querySkuRequest.getSkuIdsByType().entrySet()) {
                for (String id : entry.getValue()) {
                    try {
                        result.add(tryGettingSkuFromCache(entry.getKey(), id));
                    } catch (Throwable t) {
                        Timber.e(t, "Failed to get sku from cache!");
                    }
                }
            }
            return result;

        }).continueWith(task ->
        {
            SkuListener skuListener = querySkuRequest.getSkuListener();
            if (skuListener != null) {
                if (task.getError() != null) {
                    skuListener.onLoadingSkusFailed(new LoadingSkuFailedException(task.getError()));
                } else {
                    List<Sku> skus = task.getResult();
                    if (skus != null && !skus.isEmpty()) {
                        skuListener.onSkuLoaded(skus);
                    } else {
                        skuListener.onLoadingSkusFailed(e);
                    }
                }
            }
            return null;
        });
    }

    private Sku tryGettingSkuFromCache(SkuType skuType, String skuId) {
        String json = sharedPreferences.getString(createSkuKey(skuType, skuId), "");
        if (json != null && !json.isEmpty()) {
            return gson.fromJson(json, Sku.class);
        }
        throw new RuntimeException("Sku of type=" + skuType + " and id=" + skuId + " is not cached!");
    }

    private String createSkuKey(SkuType skuType, String id) {
        return "pref_sku_" + skuType.getValue() + "_" + id;
    }

    @Override
    public void queryPurchases(QueryPurchasesRequest queryPurchasesRequest) {
        billingr.queryPurchases(queryPurchasesRequest);
    }

    @Override
    public boolean launchPurchaseFlow(Activity activity, Sku sku) {
        return billingr.launchPurchaseFlow(activity, sku);
    }

    @Override
    public void destroy() {
        billingr.destroy();
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
    }

    @Override
    public void fetchInAppSkuDetails(List<String> skus, SkuDetailsFetcher.Listener listener) {

    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, SkuPurchaser.Listener listener) {

    }

    @Override
    public void addObserver(PurchaseListener observer) {

    }

    @Override
    public void removeObserver(PurchaseListener observer) {

    }

    @Override
    public void forEachObserver(Consumer<PurchaseListener> observerConsumer) {

    }

    private class CacheSkuListener implements SkuListener {

        private final QuerySkuRequest querySkuRequest;

        private CacheSkuListener(QuerySkuRequest querySkuRequest) {
            this.querySkuRequest = querySkuRequest;
        }

        @Override
        public void onSkuLoaded(List<Sku> skus) {
            SkuListener skuListener = querySkuRequest.getSkuListener();
            if (skuListener != null) {
                skuListener.onSkuLoaded(skus);
            }
            cache(skus);
        }

        @Override
        public void onLoadingSkusFailed(LoadingSkuFailedException e) {
            querySkusFromCache(querySkuRequest, e);
        }
    }
}
