package com.abatra.billingr.google;

import com.abatra.billingr.BillingAvailabilityResult;

public class GoogleBillingAvailabilityResult implements BillingAvailabilityResult {

    private final GoogleBillingResult googleBillingResult;

    public GoogleBillingAvailabilityResult(GoogleBillingResult googleBillingResult) {
        this.googleBillingResult = googleBillingResult;
    }

    @Override
    public boolean isOk() {
        return googleBillingResult.isOk();
    }

    @Override
    public boolean isAvailable() {
        return googleBillingResult.isAvailable();
    }
}
