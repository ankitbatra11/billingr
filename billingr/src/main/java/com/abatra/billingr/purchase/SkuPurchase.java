package com.abatra.billingr.purchase;

public interface SkuPurchase {

    String getSku();

    String getPurchaseToken();

    boolean isPurchased();

    boolean isAcknowledged();
}
