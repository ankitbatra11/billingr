package com.abatra.billingr;

public interface Sku {

    String getId();

    SkuType getType();

    String getTitle();

    String getCurrency();

    long getPriceAmount();

    long getPriceAmountMicros();
}
