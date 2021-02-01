package com.abatra.billingr;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.google.GoogleBillingr;
import com.abatra.billingr.google.GooglePurchaseFetcher;
import com.abatra.billingr.google.GoogleSkuDetailsFetcher;
import com.abatra.billingr.google.GoogleSkuPurchaser;
import com.abatra.billingr.google.InitializedBillingClientSupplier;
import com.abatra.billingr.load.LoadBillingRequest;
import com.abatra.billingr.purchase.QueryPurchasesRequest;
import com.abatra.billingr.sku.QuerySkuRequest;
import com.abatra.billingr.sku.Sku;

public interface Billingr extends LifecycleObserver, SkuDetailsFetcher, PurchaseFetcher, SkuPurchaser, ILifecycleObserver {

    static Billingr google(Context context) {
        InitializedBillingClientSupplier billingClientSupplier = new InitializedBillingClientSupplier(context);
        GooglePurchaseFetcher purchaseFetcher = new GooglePurchaseFetcher(billingClientSupplier);
        GoogleSkuDetailsFetcher skuDetailsFetcher = new GoogleSkuDetailsFetcher(billingClientSupplier);
        GoogleSkuPurchaser skuPurchaser = new GoogleSkuPurchaser(billingClientSupplier);
        return new GoogleBillingr(context, billingClientSupplier, purchaseFetcher, skuDetailsFetcher, skuPurchaser);
    }

    void loadBilling(LoadBillingRequest loadBillingRequest);

    void querySkus(QuerySkuRequest querySkuRequest);

    void queryPurchases(QueryPurchasesRequest queryPurchasesRequest);

    boolean launchPurchaseFlow(Activity activity, Sku sku);

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void destroy();
}
