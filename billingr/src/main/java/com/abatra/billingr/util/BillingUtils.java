package com.abatra.billingr.util;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.google.common.base.MoreObjects;

public final class BillingUtils {

    private BillingUtils() {
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

    public static boolean isPurchased(Purchase purchase) {
        return purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED;
    }
}
