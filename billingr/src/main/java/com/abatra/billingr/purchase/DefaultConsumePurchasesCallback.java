package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

public interface DefaultConsumePurchasesCallback extends ConsumePurchasesCallback {

    DefaultConsumePurchasesCallback INSTANCE = new DefaultConsumePurchasesCallback() {
    };

    @Override
    default void onPurchasesConsumptionFailure(BillingrException billingrException) {
    }

    @Override
    default void onPurchasesConsumptionResultUpdated(PurchasesConsumptionResult purchasesConsumptionResult) {
    }

    @Override
    default void onBillingUnavailable() {
    }
}
