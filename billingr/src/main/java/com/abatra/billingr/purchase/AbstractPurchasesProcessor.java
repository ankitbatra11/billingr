package com.abatra.billingr.purchase;

import java.util.List;

import timber.log.Timber;

abstract class AbstractPurchasesProcessor implements PurchasesProcessor {

    @Override
    public void processPurchases(List<SkuPurchase> purchases, Listener listener) {
        for (SkuPurchase skuPurchase : purchases) {
            try {
                tryHandlingPurchase(skuPurchase, listener);
            } catch (Exception exception) {
                Timber.e(exception, "Acknowledging purchase=%s failed!", skuPurchase);
            }
        }
    }

    protected abstract void tryHandlingPurchase(SkuPurchase skuPurchase, Listener listener);
}
