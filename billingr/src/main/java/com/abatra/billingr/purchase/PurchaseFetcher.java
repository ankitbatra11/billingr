package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;

public interface PurchaseFetcher extends ILifecycleObserver {
    void fetchInAppPurchases(PurchaseListener listener);
}
