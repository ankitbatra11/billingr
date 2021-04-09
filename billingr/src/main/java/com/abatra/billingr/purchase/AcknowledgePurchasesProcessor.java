package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;

public class AcknowledgePurchasesProcessor extends AbstractPurchasesProcessor {

    private final PurchaseAcknowledger purchaseAcknowledger;

    public AcknowledgePurchasesProcessor(PurchaseAcknowledger purchaseAcknowledger) {
        this.purchaseAcknowledger = purchaseAcknowledger;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        purchaseAcknowledger.observeLifecycle(lifecycleOwner);
    }

    @Override
    protected void tryHandlingPurchase(SkuPurchase skuPurchase, Listener listener) {
        purchaseAcknowledger.acknowledgePurchase(skuPurchase, new PurchaseAcknowledger.Callback() {

            @Override
            public void onPurchaseAcknowledged() {
                listener.onProcessed(skuPurchase);
            }

            @Override
            public void onPurchaseAcknowledgeFailed(BillingrException billingrException) {
                listener.onProcessingFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                listener.onProcessingFailed(BillingrException.unavailable());
            }
        });
    }
}
