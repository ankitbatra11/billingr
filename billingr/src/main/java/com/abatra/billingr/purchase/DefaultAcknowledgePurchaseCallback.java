package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

public interface DefaultAcknowledgePurchaseCallback extends AcknowledgePurchaseCallback {

    DefaultAcknowledgePurchaseCallback INSTANCE = new DefaultAcknowledgePurchaseCallback() {
    };

    @Override
    default void onPurchaseAcknowledged(SkuPurchase skuPurchase) {

    }

    @Override
    default void onPurchaseAcknowledgeFailed(SkuPurchase skuPurchase, BillingrException billingrException) {

    }

    @Override
    default void onBillingUnavailable() {

    }
}
