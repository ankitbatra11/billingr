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

public class BillingrCache implements Billingr {

    private static final String LOG_TAG = "BillingrCache";

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
                    try {
                        result.add(tryGettingSkuFromCache(entry.getKey(), id));
                    } catch (Throwable t) {
                        Log.e(LOG_TAG, "Failed to get sku from cache!", t);
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
    public void purchase(Activity activity, Sku sku) {
        billingr.purchase(activity, sku);
    }

    @Override
    public void destroy() {
        billingr.destroy();
    }
}
