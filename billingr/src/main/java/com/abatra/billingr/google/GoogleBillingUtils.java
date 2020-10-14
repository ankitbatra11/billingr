package com.abatra.billingr.google;

import android.util.Log;

import com.abatra.billingr.purchase.Purchase;
import com.abatra.billingr.sku.SkuType;
import com.android.billingclient.api.BillingClient;

public class GoogleBillingUtils {

    private static final String LOG_TAG = "GoogleBillingUtils";

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

    /**
     * @param inAppProductName Premium (SCAR)
     * @return Premium
     */
    public static String removeAppName(String inAppProductName) {
        String result = inAppProductName;
        int indexOf = inAppProductName.indexOf('(');
        if (indexOf != -1) {
            try {
                result = inAppProductName.substring(0, indexOf).trim();
            } catch (Throwable e) {
                Log.e(LOG_TAG, "Removing appName failed for sku=" + inAppProductName, e);
            }
        }
        return result;
    }
}
