package com.abatra.billingr.google;

import com.abatra.billingr.Purchase;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuType;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.SkuDetails;

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

    public static Sku toSku(SkuDetails skuDetails) {
        Sku sku = new Sku();
        sku.setCurrency(skuDetails.getPriceCurrencyCode());
        sku.setPrice(skuDetails.getPriceAmountMicros());
        sku.setTitle(skuDetails.getTitle());
        return sku;
    }

    public static Purchase toPurchase(PurchaseHistoryRecord purchaseHistoryRecord) {
        Purchase purchase = new Purchase();
        purchase.setSku(purchaseHistoryRecord.getSku());
        return purchase;
    }
}
