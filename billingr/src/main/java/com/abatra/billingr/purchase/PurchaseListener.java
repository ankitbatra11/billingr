package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

import java.util.List;

public interface PurchaseListener extends BillingUnavailableCallback {

    void onBillingUnavailable();

    void onPurchasesLoaded(List<SkuPurchase> skuPurchases);

    void onPurchasesLoadFailed(BillingrException error);

    interface NoOpPurchaseListener extends PurchaseListener {

        @Override
        default void onBillingUnavailable() {
        }

        @Override
        default void onPurchasesLoaded(List<SkuPurchase> skuPurchases) {
        }

        @Override
        default void onPurchasesLoadFailed(BillingrException error) {
        }
    }
}
