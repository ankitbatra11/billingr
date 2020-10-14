package com.abatra.billingr.sku;

import java.util.List;

public interface SkuPurchaseListener {
    void onPurchaseStatusChanged(List<SkuPurchase> skuPurchases);
}
