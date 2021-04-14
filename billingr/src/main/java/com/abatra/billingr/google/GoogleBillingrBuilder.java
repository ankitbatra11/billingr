package com.abatra.billingr.google;

import android.content.Context;

import com.abatra.billingr.AbstractBillingrBuilder;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.analytics.AnalyticsSkuPurchaser;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.function.Function;

public class GoogleBillingrBuilder extends AbstractBillingrBuilder {

    private final Context context;
    private boolean enablePendingPurchases;

    public GoogleBillingrBuilder(Context context) {
        this.context = context;
    }

    public GoogleBillingrBuilder withPendingPurchasesEnabled(boolean enablePendingPurchases) {
        this.enablePendingPurchases = enablePendingPurchases;
        return this;
    }

    @Override
    protected Billingr build(boolean analyticsEnabled) {
        InitializedBillingClientSupplier billingClientSupplier = new InitializedBillingClientSupplier(createBillingClientFactoryMethod());
        GoogleSkuPurchaser googleSkuPurchaser = new GoogleSkuPurchaser(billingClientSupplier);
        return new GoogleBillingr(
                new GooglePurchaseFetcher(billingClientSupplier),
                new GoogleSkuDetailsFetcher(billingClientSupplier),
                analyticsEnabled ? new AnalyticsSkuPurchaser(googleSkuPurchaser) : googleSkuPurchaser,
                new GoogleBillingAvailabilityChecker(billingClientSupplier),
                new GooglePurchaseConsumer(billingClientSupplier),
                new GooglePurchaseAcknowledger(billingClientSupplier)
        );
    }

    private Function<PurchasesUpdatedListener, BillingClient> createBillingClientFactoryMethod() {
        return purchasesUpdatedListener -> {
            BillingClient.Builder builder = BillingClient.newBuilder(context);
            builder.setListener(purchasesUpdatedListener);
            if (enablePendingPurchases) {
                builder.enablePendingPurchases();
            }
            return builder.build();
        };
    }
}
