package com.abatra.billingr;

public interface PurchaseFetcher {

    void fetchInAppPurchases(PurchaseListener listener);

    void fetchUnacknowledgedInAppPurchases(PurchaseListener listener);

}
