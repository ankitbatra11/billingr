package com.abatra.billingr;

import com.android.billingclient.api.BillingResult;

public class GoogleLaunchPurchaseFlowResult implements LaunchPurchaseFlowResult {

    private final BillingResult billingResult;

    public GoogleLaunchPurchaseFlowResult(BillingResult billingResult) {
        this.billingResult = billingResult;
    }


}
