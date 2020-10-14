package com.abatra.billingr.sku;

public class SkuPurchase {

    private final Sku sku;

    public SkuPurchase(Sku sku) {
        this.sku = sku;
    }

    public Sku getSku() {
        return sku;
    }
}
