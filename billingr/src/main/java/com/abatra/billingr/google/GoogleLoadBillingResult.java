package com.abatra.billingr.google;

import com.abatra.billingr.load.LoadBillingResult;

public class GoogleLoadBillingResult implements LoadBillingResult {

    private final GoogleBillingResult googleBillingResult;

    public GoogleLoadBillingResult(GoogleBillingResult googleBillingResult) {
        this.googleBillingResult = googleBillingResult;
    }

    @Override
    public boolean isLoadedSuccessfully() {
        return googleBillingResult.isOk();
    }

    @Override
    public boolean isBillingAvailable() {
        return googleBillingResult.isAvailable();
    }
}
