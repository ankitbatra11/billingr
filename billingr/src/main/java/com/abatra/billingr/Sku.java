package com.abatra.billingr;

import com.google.common.base.MoreObjects;

public interface Sku {

    String AFFILIATION_GOOGLE = "google";

    String getId();

    SkuType getType();

    String getTitle();

    String getCurrency();

    double getPriceAmount();

    long getPriceAmountMicros();

    String getOriginalJson();

    String getAffiliation();

    static String toString(Sku sku) {
        return MoreObjects.toStringHelper(sku)
                .add("id", sku.getId())
                .add("type", sku.getType())
                .add("title", sku.getTitle())
                .add("currency", sku.getCurrency())
                .add("priceAmount", sku.getPriceAmount())
                .add("priceAmountMicros", sku.getPriceAmountMicros())
                .toString();
    }
}
