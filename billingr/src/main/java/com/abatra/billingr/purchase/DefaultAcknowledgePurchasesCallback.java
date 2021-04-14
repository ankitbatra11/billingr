package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

public interface DefaultAcknowledgePurchasesCallback extends AcknowledgePurchasesCallback {

    DefaultAcknowledgePurchasesCallback INSTANCE = new DefaultAcknowledgePurchasesCallback() {
    };

    @Override
    default void onPurchasesAcknowledgeFailure(BillingrException error) {

    }

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
