package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

public interface PurchaseAcknowledger extends ILifecycleObserver {

    void acknowledgePurchase(SkuPurchase skuPurchase, Callback callback);

    interface Callback extends BillingUnavailableCallback {

        void onPurchaseAcknowledged();

        void onPurchaseAcknowledgeFailed(BillingrException billingrException);
    }

    interface NoOpCallback extends Callback {

        @Override
        default void onPurchaseAcknowledged() {

        }

        @Override
        default void onPurchaseAcknowledgeFailed(BillingrException billingrException) {

        }

        @Override
        default void onBillingUnavailable() {

        }
    }
}
