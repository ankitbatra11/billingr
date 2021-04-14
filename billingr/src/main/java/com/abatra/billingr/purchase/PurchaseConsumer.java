package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;

import java.util.List;

public interface PurchaseConsumer extends ILifecycleObserver {

    void consumePurchase(SkuPurchase skuPurchase, ConsumePurchaseCallback callback);

    void consumePurchases(List<SkuPurchase> skuPurchases, ConsumePurchasesCallback callback);

}
