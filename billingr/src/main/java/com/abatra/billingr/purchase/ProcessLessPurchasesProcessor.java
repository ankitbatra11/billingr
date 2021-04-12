package com.abatra.billingr.purchase;

import java.util.List;

import timber.log.Timber;

public class ProcessLessPurchasesProcessor implements PurchasesProcessor {

    public static final ProcessLessPurchasesProcessor INSTANCE = new ProcessLessPurchasesProcessor();

    private ProcessLessPurchasesProcessor() {
    }

    @Override
    public void processPurchases(List<SkuPurchase> purchases, Listener listener) {
        for (SkuPurchase skuPurchase : purchases) {
            try {
                listener.onProcessed(skuPurchase);
            } catch (Throwable t) {
                Timber.e(t, "listener=%s failed to process purchase=%s", listener, skuPurchase);
            }
        }
    }
}
