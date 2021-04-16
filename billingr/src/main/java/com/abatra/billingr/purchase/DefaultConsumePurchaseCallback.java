package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

public interface DefaultConsumePurchaseCallback extends ConsumePurchaseCallback {

    DefaultConsumePurchaseCallback INSTANCE = new DefaultConsumePurchaseCallback() {
    };

    @Override
    default void onPurchaseConsumed(SkuPurchase skuPurchase) {

    }

    @Override
    default void onPurchaseConsumptionFailed(SkuPurchase skuPurchase, BillingrException billingrException) {

    }

    @Override
    default void onBillingUnavailable() {

    }
}
