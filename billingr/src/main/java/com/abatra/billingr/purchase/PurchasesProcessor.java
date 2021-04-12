package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.BillingrException;

import java.util.List;

public interface PurchasesProcessor extends ILifecycleObserver {

    void processPurchases(List<SkuPurchase> purchases, Listener listener);

    interface Listener {

        void onProcessed(SkuPurchase skuPurchase);

        void onProcessingFailed(BillingrException billingrException);
    }
}
