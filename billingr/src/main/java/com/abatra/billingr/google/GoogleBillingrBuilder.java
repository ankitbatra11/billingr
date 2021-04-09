package com.abatra.billingr.google;

import android.content.Context;

import com.abatra.billingr.AbstractBillingrBuilder;
import com.abatra.billingr.BillingAvailabilityChecker;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.analytics.AnalyticsSkuPurchaser;
import com.abatra.billingr.purchase.SkuPurchaser;

public class GoogleBillingrBuilder extends AbstractBillingrBuilder {

    private final Context context;

    public GoogleBillingrBuilder(Context context) {
        this.context = context;
    }

    @Override
    public Billingr build() {
        BillingClientFactory billingClientFactory = new BillingClientFactory(context);
        InitializedBillingClientSupplier billingClientSupplier = new InitializedBillingClientSupplier(billingClientFactory);
        GooglePurchaseFetcher purchaseFetcher = new GooglePurchaseFetcher(billingClientSupplier);
        GoogleSkuDetailsFetcher skuDetailsFetcher = new GoogleSkuDetailsFetcher(billingClientSupplier);
        SkuPurchaser skuPurchaser = new GoogleSkuPurchaser(billingClientSupplier);
        if (analyticsEnabled) {
            skuPurchaser = new AnalyticsSkuPurchaser(skuPurchaser);
        }
        BillingAvailabilityChecker availabilityChecker = new GoogleBillingAvailabilityChecker(billingClientSupplier);
        return new GoogleBillingr(billingClientSupplier, purchaseFetcher, skuDetailsFetcher, skuPurchaser, availabilityChecker);
    }
}
