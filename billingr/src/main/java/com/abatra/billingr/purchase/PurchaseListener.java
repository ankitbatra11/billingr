package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

import java.util.List;

public interface PurchaseListener extends BillingUnavailableCallback {

    static PurchaseListener noOp() {
        return NoOpPurchaseListener.INSTANCE;
    }

    void onBillingUnavailable();

    void onPurchasesUpdated(List<SkuPurchase> skuPurchases);

    void onPurchasesUpdateFailed(BillingrException error);

    interface NoOpPurchaseListener extends PurchaseListener {

        NoOpPurchaseListener INSTANCE = new NoOpPurchaseListener() {
        };

        @Override
        default void onBillingUnavailable() {
        }

        @Override
        default void onPurchasesUpdated(List<SkuPurchase> skuPurchases) {
        }

        @Override
        default void onPurchasesUpdateFailed(BillingrException error) {
        }
    }
}
