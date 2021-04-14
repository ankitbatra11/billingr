package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

public interface ConsumePurchaseCallback extends BillingUnavailableCallback {

    void onPurchaseConsumed(SkuPurchase skuPurchase);

    void onPurchaseConsumptionFailed(SkuPurchase skuPurchase, BillingrException billingrException);
}
