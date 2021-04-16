package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurchasesConsumptionResult implements DefaultConsumePurchaseCallback {

    private final List<SkuPurchase> purchasesToConsume;
    private final List<SkuPurchase> consumedPurchases = Collections.synchronizedList(new ArrayList<>());
    private final List<SkuPurchase> failedToConsumePurchases = Collections.synchronizedList(new ArrayList<>());

    public PurchasesConsumptionResult(List<SkuPurchase> purchasesToConsume) {
        this.purchasesToConsume = purchasesToConsume;
    }

    @Override
    public void onPurchaseConsumed(SkuPurchase skuPurchase) {
        consumedPurchases.add(skuPurchase);
    }

    @Override
    public void onPurchaseConsumptionFailed(SkuPurchase skuPurchase, BillingrException billingrException) {
        failedToConsumePurchases.add(skuPurchase);
    }

    public List<SkuPurchase> getPurchasesToConsume() {
        return purchasesToConsume;
    }

    public List<SkuPurchase> getConsumedPurchases() {
        return consumedPurchases;
    }

    public List<SkuPurchase> getFailedToConsumePurchases() {
        return failedToConsumePurchases;
    }

    public boolean isComplete() {
        return purchasesToConsume.size() == (consumedPurchases.size() + failedToConsumePurchases.size());
    }
}
