package com.abatra.billingr.google;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;

import java.util.Locale;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.BILLING_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;

public class GoogleBillingResult {

    private final BillingResult billingResult;

    private GoogleBillingResult(BillingResult billingResult) {
        this.billingResult = billingResult;
    }

    public static GoogleBillingResult wrap(BillingResult billingResult) {
        return new GoogleBillingResult(billingResult);
    }

    public boolean isOk() {
        return billingResult.getResponseCode() == OK;
    }

    public boolean isAvailable() {
        return billingResult.getResponseCode() != BILLING_UNAVAILABLE;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "GoogleBillingResult: responseCode=%d debugMessage=%s",
                billingResult.getResponseCode(), billingResult.getDebugMessage());
    }
}
