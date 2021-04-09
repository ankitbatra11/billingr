package com.abatra.billingr.google;

import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.SkuPurchase;
import com.abatra.billingr.sku.SkuType;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import timber.log.Timber;

public final class GoogleBillingUtils {

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

    public static BillingrException reportErrorAndGet(BillingResult billingResult, String message, Object... args) {
        BillingrException billingrException = GoogleBillingrException.from(billingResult);
        Timber.e(billingrException, message, args);
        return billingrException;
    }

    public static List<SkuPurchase> toSkuPurchases(List<Purchase> purchases) {
        return Optional.ofNullable(purchases)
                .orElse(Collections.emptyList())
                .stream()
                .map(GoogleSkuPurchase::new)
                .collect(Collectors.toList());
    }
}
