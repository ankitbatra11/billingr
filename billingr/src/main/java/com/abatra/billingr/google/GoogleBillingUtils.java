package com.abatra.billingr.google;

import com.abatra.billingr.SkuType;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.google.common.base.MoreObjects;

import timber.log.Timber;

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
                Timber.e(e, "Removing appName failed for sku=%s", inAppProductName);
            }
        }
        return result;
    }

    public static boolean isOk(BillingResult billingResult) {
        return billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK;
    }

    public static String toString(BillingResult billingResult) {
        return MoreObjects.toStringHelper(billingResult)
                .add("responseCode", billingResult.getResponseCode())
                .add("debugMessage", billingResult.getDebugMessage())
                .toString();
    }

    public static boolean isPurchased(com.android.billingclient.api.Purchase purchase) {
        return purchase.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED;
    }

    public static boolean isError(BillingResult billingResult) {
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.ERROR:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return true;
        }
        return false;
    }

    public static boolean isUnavailable(BillingResult billingResult) {
        return billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE;
    }
}
