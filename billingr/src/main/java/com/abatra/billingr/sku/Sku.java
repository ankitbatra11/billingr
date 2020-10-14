package com.abatra.billingr.sku;

import java.util.Locale;

public interface Sku {

    String AFFILIATION_GOOGLE = "affiliation_google";

    String getId();

    SkuType getType();

    String getTitle();

    String getCurrency();

    long getPriceAmount();

    long getPriceAmountMicros();

    String getJson();

    String getAffiliation();

    static String toString(Sku sku) {
        return String.format(Locale.ENGLISH,
                "Sku(id=%s, type=%s title=%s currency=%s price=%d priceMicros=%d)",
                sku.getId(), sku.getType(), sku.getTitle(), sku.getCurrency(), sku.getPriceAmount(), sku.getPriceAmountMicros());
    }
}
