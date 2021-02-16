package com.abatra.billingr;

import android.content.Context;

import androidx.lifecycle.LifecycleObserver;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.google.BillingClientFactory;
import com.abatra.billingr.google.GoogleBillingr;
import com.abatra.billingr.google.GooglePurchaseFetcher;
import com.abatra.billingr.google.GoogleSkuDetailsFetcher;
import com.abatra.billingr.google.GoogleSkuPurchaser;
import com.abatra.billingr.google.InitializedBillingClientSupplier;

public interface Billingr extends LifecycleObserver, SkuDetailsFetcher, PurchaseFetcher, SkuPurchaser, ILifecycleObserver {

    static Billingr google(Context context) {
        BillingClientFactory billingClientFactory = new BillingClientFactory(context);
        InitializedBillingClientSupplier billingClientSupplier = new InitializedBillingClientSupplier(billingClientFactory);
        GooglePurchaseFetcher purchaseFetcher = new GooglePurchaseFetcher(billingClientSupplier);
        GoogleSkuDetailsFetcher skuDetailsFetcher = new GoogleSkuDetailsFetcher(billingClientSupplier);
        GoogleSkuPurchaser skuPurchaser = new GoogleSkuPurchaser(billingClientSupplier);
        return new GoogleBillingr(billingClientSupplier, purchaseFetcher, skuDetailsFetcher, skuPurchaser);
    }
}
