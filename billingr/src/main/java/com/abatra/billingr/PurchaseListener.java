package com.abatra.billingr;

import java.util.List;

public interface PurchaseListener {

    default void updated(List<SkuPurchase> skuPurchases) {
    }

    default void loadingPurchasesFailed(BillingrException error) {
    }
}
