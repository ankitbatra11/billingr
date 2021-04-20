package com.abatra.billingr;


import com.abatra.android.wheelie.lifecycle.observer.ILifecycleObserver;
import com.abatra.billingr.purchase.ConsumePurchasesCallback;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuDetailsFetcher;

public interface Billingr extends ILifecycleObserver, SkuDetailsFetcher, PurchaseFetcher, SkuPurchaser,
        BillingAvailabilityChecker, PurchaseConsumer, PurchaseAcknowledger {

    void acknowledgeInAppPurchases();

    void consumeInAppPurchases();

    void consumePurchase(Sku sku, ConsumePurchasesCallback consumePurchasesCallback);
}
