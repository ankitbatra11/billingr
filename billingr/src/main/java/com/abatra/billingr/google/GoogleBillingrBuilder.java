package com.abatra.billingr.google;

import android.content.Context;

import com.abatra.billingr.AbstractBillingrBuilder;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.analytics.AnalyticsSkuPurchaser;

public class GoogleBillingrBuilder extends AbstractBillingrBuilder {

    private final Context context;

    public GoogleBillingrBuilder(Context context) {
        this.context = context;
    }

    @Override
    protected Billingr build(boolean analyticsEnabled) {
        BillingClientFactory billingClientFactory = new BillingClientFactory(context);
        InitializedBillingClientSupplier billingClientSupplier = new InitializedBillingClientSupplier(billingClientFactory);
        GoogleSkuPurchaser googleSkuPurchaser = new GoogleSkuPurchaser(billingClientSupplier);
        return new GoogleBillingr(billingClientSupplier,
                new GooglePurchaseFetcher(billingClientSupplier),
                new GoogleSkuDetailsFetcher(billingClientSupplier),
                analyticsEnabled ? new AnalyticsSkuPurchaser(googleSkuPurchaser) : googleSkuPurchaser,
                new GoogleBillingAvailabilityChecker(billingClientSupplier),
                new GooglePurchaseConsumer(billingClientSupplier),
                new GooglePurchaseAcknowledger(billingClientSupplier));
    }
}
