package com.abatra.billingr.google;

import androidx.annotation.NonNull;

import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuType;
import com.android.billingclient.api.SkuDetails;

public class GoogleSku implements Sku {

    private final SkuType skuType;
    private final SkuDetails skuDetails;

    public GoogleSku(SkuType skuType, SkuDetails skuDetails) {
        this.skuType = skuType;
        this.skuDetails = skuDetails;
    }

    public SkuDetails getSkuDetails() {
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
    public long getPriceAmount() {
        return Double.valueOf(getPriceAmountMicros() / Math.pow(10, 6)).longValue();
    }

    @Override
    public long getPriceAmountMicros() {
        return skuDetails.getPriceAmountMicros();
    }

    @Override
    public String getOriginalJson() {
        return skuDetails.getOriginalJson();
    }

    @Override
    public String getAffiliation() {
        return Sku.AFFILIATION_GOOGLE;
    }

    @NonNull
    @Override
    public String toString() {
        return Sku.toString(this);
    }
}
