package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.sku.Sku;

public interface PurchaseFetcher extends ILifecycleObserver {
    void fetchInAppPurchases(PurchaseListener listener);
}
