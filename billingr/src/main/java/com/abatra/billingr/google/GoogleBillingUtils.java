package com.abatra.billingr.google;

import com.abatra.billingr.Purchase;
import com.abatra.billingr.SkuType;
import com.android.billingclient.api.BillingClient;

public class GoogleBillingUtils {

    private GoogleBillingUtils() {
    }

    public static String getSkuType(SkuType skuType) {
        switch (skuType) {
            case IN_APP_PRODUCT:
                return BillingClient.SkuType.INAPP;
            case SUBSCRIPTION:
                return BillingClient.SkuType.SUBS;
        }
        throw new IllegalArgumentException("Invalid skuType=" + skuType);
    }

    public static Purchase toPurchase(com.android.billingclient.api.Purchase purchase) {
        return new Purchase(purchase.getSku(), purchase.getOrderId());
    }
}
