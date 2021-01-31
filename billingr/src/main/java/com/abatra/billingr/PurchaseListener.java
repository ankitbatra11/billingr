package com.abatra.billingr;

import java.util.List;

public interface PurchaseListener {
    void updated(List<SkuPurchase> skuPurchases);
}
