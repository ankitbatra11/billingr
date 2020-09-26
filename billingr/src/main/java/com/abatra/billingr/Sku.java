package com.abatra.billingr;

public interface Sku {

    SkuType getType();

    String getTitle();

    String getCurrency();

    long getPrice();
}
