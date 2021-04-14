package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

public interface AcknowledgePurchaseCallback extends BillingUnavailableCallback {

    void onPurchaseAcknowledged(SkuPurchase skuPurchase);

    void onPurchaseAcknowledgeFailed(SkuPurchase skuPurchase, BillingrException billingrException);
}
