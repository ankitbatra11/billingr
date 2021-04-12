package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

public interface PurchaseConsumer extends ILifecycleObserver {

    void consumePurchase(SkuPurchase skuPurchase, Callback callback);

    interface Callback extends BillingUnavailableCallback {

        void onPurchaseConsumed();

        void onPurchaseConsumeFailed(BillingrException billingrException);
    }

    interface NoOpCallback extends Callback {

        NoOpCallback INSTANCE = new NoOpCallback() {
        };

        @Override
        default void onPurchaseConsumed() {
        }

        @Override
        default void onPurchaseConsumeFailed(BillingrException billingrException) {
        }

        @Override
        default void onBillingUnavailable() {
        }
    }
}
