package com.abatra.billingr;

import java.util.List;

public interface PurchaseListener extends BillingAvailabilityCallback {

    @Override
    default void onBillingUnavailable() {

    }

    default void updated(List<SkuPurchase> skuPurchases) {
    }

    default void loadingPurchasesFailed(BillingrException error) {
    }
}
