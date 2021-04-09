package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;

public class ConsumePurchasesProcessor extends AbstractPurchasesProcessor {

    private final PurchaseConsumer purchaseConsumer;

    public ConsumePurchasesProcessor(PurchaseConsumer purchaseConsumer) {
        this.purchaseConsumer = purchaseConsumer;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        purchaseConsumer.observeLifecycle(lifecycleOwner);
    }

    @Override
    protected void tryHandlingPurchase(SkuPurchase skuPurchase, Listener listener) {
        purchaseConsumer.consumePurchase(skuPurchase, new PurchaseConsumer.Callback() {
            @Override
            public void onPurchaseConsumed() {
                listener.onProcessed(skuPurchase);
            }

            @Override
            public void onPurchaseConsumeFailed(BillingrException billingrException) {
                listener.onProcessingFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                listener.onProcessingFailed(BillingrException.unavailable());
            }
        });
    }
}
