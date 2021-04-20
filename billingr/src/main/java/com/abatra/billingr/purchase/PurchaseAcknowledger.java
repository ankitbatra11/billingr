package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.observer.ILifecycleObserver;

import java.util.List;

public interface PurchaseAcknowledger extends ILifecycleObserver {

    void acknowledgePurchase(SkuPurchase skuPurchase, AcknowledgePurchaseCallback callback);

    void acknowledgePurchases(List<SkuPurchase> skuPurchases, AcknowledgePurchasesCallback callback);

}
