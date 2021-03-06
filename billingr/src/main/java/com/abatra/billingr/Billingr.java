package com.abatra.billingr;

import android.content.Context;

import androidx.lifecycle.LifecycleObserver;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.analytics.FirebaseAnalyticsSkuPurchaser;
import com.abatra.billingr.google.BillingClientFactory;
import com.abatra.billingr.google.GoogleBillingr;
import com.abatra.billingr.google.GooglePurchaseFetcher;
import com.abatra.billingr.google.GoogleSkuDetailsFetcher;
import com.abatra.billingr.google.GoogleSkuPurchaser;
import com.abatra.billingr.google.InitializedBillingClientSupplier;

public interface Billingr extends LifecycleObserver, SkuDetailsFetcher, PurchaseFetcher, SkuPurchaser, ILifecycleObserver {

    class Builder {

        private final Context context;
        private boolean firebaseAnalyticsEnabled = false;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setFirebaseAnalyticsEnabled(boolean firebaseAnalyticsEnabled) {
            this.firebaseAnalyticsEnabled = firebaseAnalyticsEnabled;
            return this;
        }

        public Billingr buildForPlayStore() {
            BillingClientFactory billingClientFactory = new BillingClientFactory(context.getApplicationContext());
            InitializedBillingClientSupplier billingClientSupplier = new InitializedBillingClientSupplier(billingClientFactory);
            GooglePurchaseFetcher purchaseFetcher = new GooglePurchaseFetcher(billingClientSupplier);
            GoogleSkuDetailsFetcher skuDetailsFetcher = new GoogleSkuDetailsFetcher(billingClientSupplier);
            SkuPurchaser skuPurchaser = new GoogleSkuPurchaser(billingClientSupplier);
            if (firebaseAnalyticsEnabled) {
                skuPurchaser = new FirebaseAnalyticsSkuPurchaser(skuPurchaser);
            }
            return new GoogleBillingr(billingClientSupplier, purchaseFetcher, skuDetailsFetcher, skuPurchaser);
        }
    }
}
