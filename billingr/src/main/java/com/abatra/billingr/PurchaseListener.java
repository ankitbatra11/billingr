package com.abatra.billingr;

import com.abatra.billingr.exception.BillingrException;

import java.util.List;

public interface PurchaseListener {

    default void updated(List<SkuPurchase> skuPurchases) {
    }

    default void loadingPurchasesFailed(BillingrException error) {
    }
}
