package com.abatra.billingr.cache;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.abatra.billingr.Billingr;
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

public class CachedBillingr implements Billingr {

    private static final String LOG_TAG = "CachedBillingr";

    private final Billingr billingr;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public CachedBillingr(Billingr billingr, SharedPreferences sharedPreferences, Gson gson) {
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

        QuerySkuRequest request = QuerySkuRequest.builder()
                .forSku(querySkuRequest.getSkuIdsByType())
                .setSkuListener(new SkuListener() {
                    @Override
                    public void onSkuLoaded(List<Sku> skus) {
                        querySkuRequest.getSkuListener().onSkuLoaded(skus);
                        cache(skus);
                    }

                    @Override
                    public void onLoadingSkusFailed(LoadingSkuFailedException e) {
                        querySkusFromCache(querySkuRequest, e);
                    }
                })
                .build();

        billingr.querySkus(request);
    }

    private void cache(List<Sku> skus) {
        Task.callInBackground(() -> {
            for (Sku sku : skus) {
                try {
                    tryCaching(sku);
                } catch (Throwable t) {
                    Log.e(LOG_TAG, "Failed to cache sku=" + sku, t);
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
                    String json = sharedPreferences.getString(createSkuKey(entry.getKey(), id), "");
                    if (json != null && !json.isEmpty()) {
                        result.add(gson.fromJson(json, Sku.class));
                    }
                }
            }
            return result;

        }).continueWith(task ->
        {
            if (task.getError() != null) {
                querySkuRequest.getSkuListener().onLoadingSkusFailed(new LoadingSkuFailedException(task.getError()));
            } else {
                List<Sku> skus = task.getResult();
                if (skus != null && !skus.isEmpty()) {
                    querySkuRequest.getSkuListener().onSkuLoaded(skus);
                } else {
                    querySkuRequest.getSkuListener().onLoadingSkusFailed(e);
                }
            }
            return null;
        });
    }

    private String createSkuKey(SkuType skuType, String id) {
        return "pref_sku_" + skuType.getValue() + "_" + id;
    }

    @Override
    public void queryPurchases(QueryPurchasesRequest queryPurchasesRequest) {
        billingr.queryPurchases(queryPurchasesRequest);
    }

    @Override
    public void purchase(Activity activity, Sku sku) {
        billingr.purchase(activity, sku);
    }

    @Override
    public void destroy() {
        billingr.destroy();
    }
}
