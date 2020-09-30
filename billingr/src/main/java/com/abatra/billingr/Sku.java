package com.abatra.billingr;

import java.util.Locale;

public interface Sku {

    static String toString(Sku sku) {
        return String.format(Locale.ENGLISH,
                "GoogleSku(id=%s, type=%s title=%s currency=%s price=%d priceMicros=%d)",
                sku.getId(), sku.getType(), sku.getTitle(), sku.getCurrency(), sku.getPriceAmount(), sku.getPriceAmountMicros());
    }

    String getId();

    SkuType getType();

    String getTitle();

    String getCurrency();

    long getPriceAmount();

    long getPriceAmountMicros();
}
