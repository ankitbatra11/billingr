package com.abatra.billingr;

import java.util.List;

public interface SkuPurchaseListener {
    void onPurchaseStatusChanged(List<SkuPurchase> skuPurchases);
}
