package com.abatra.billingr;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.sku.SkuDetailsFetcher;

public interface Billingr extends ILifecycleObserver, SkuDetailsFetcher, PurchaseFetcher, SkuPurchaser,
        BillingAvailabilityChecker, PurchaseConsumer, PurchaseAcknowledger {
}
