package com.abatra.billingr;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.abatra.billingr.cache.BillingrCache;
import com.abatra.billingr.google.GoogleBillingr;
import com.abatra.billingr.gson.GsonFactory;
import com.abatra.billingr.load.LoadBillingRequest;
import com.abatra.billingr.purchase.QueryPurchasesRequest;
import com.abatra.billingr.sku.QuerySkuRequest;
import com.abatra.billingr.sku.Sku;

public interface Billingr extends LifecycleObserver {

    static Billingr google(Context context) {
        return new GoogleBillingr(context);
    }

    static Billingr cachedGoogle(Context context) {
        return cached(context, google(context));
    }

    static Billingr cached(Context context, Billingr billingr) {
        return new BillingrCache(billingr,
                PreferenceManager.getDefaultSharedPreferences(context),
                GsonFactory.createGson());
    }

    void loadBilling(LoadBillingRequest loadBillingRequest);

    void querySkus(QuerySkuRequest querySkuRequest);

    void queryPurchases(QueryPurchasesRequest queryPurchasesRequest);

    void purchase(Activity activity, Sku sku);

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void destroy();
}
