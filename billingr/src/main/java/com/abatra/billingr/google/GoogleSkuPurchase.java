package com.abatra.billingr.google;

import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.Purchase;

public class GoogleSkuPurchase implements SkuPurchase {

    private final Purchase purchase;

    public GoogleSkuPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    @Override
    public String getSku() {
        return purchase.getSku();
    }

    @Override
    public String getPurchaseToken() {
        return purchase.getPurchaseToken();
    }

    public Purchase getPurchase() {
        return purchase;
    }
}
