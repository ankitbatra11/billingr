package com.abatra.billingr.google;

import androidx.annotation.NonNull;

import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuType;
import com.android.billingclient.api.SkuDetails;

import java.util.Locale;

public class GoogleSku implements Sku {

    private final SkuType skuType;
    private final SkuDetails skuDetails;

    public GoogleSku(SkuType skuType, SkuDetails skuDetails) {
        this.skuType = skuType;
        this.skuDetails = skuDetails;
    }

    SkuDetails getSkuDetails() {
        return skuDetails;
    }

    @Override
    public String getId() {
        return skuDetails.getSku();
    }

    @Override
    public SkuType getType() {
        return skuType;
    }

    @Override
    public String getTitle() {
        return skuDetails.getTitle();
    }

    @Override
    public String getCurrency() {
        return skuDetails.getPriceCurrencyCode();
    }

    @Override
    public long getPrice() {
        return Double.valueOf(skuDetails.getPriceAmountMicros() / Math.pow(10, 6)).longValue();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "GoogleSku(id=%s, type=%s title=%s currency=%s price=%d)",
                getId(), getType(), getTitle(), getCurrency(), getPrice());
    }
}
