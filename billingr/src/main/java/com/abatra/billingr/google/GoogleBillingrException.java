package com.abatra.billingr.google;

import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingResult;

public class GoogleBillingrException extends BillingrException {

    public GoogleBillingrException(String message) {
        super(message);
    }

    public static GoogleBillingrException from(BillingResult billingResult) {
        return new GoogleBillingrException(GoogleBillingUtils.toString(billingResult));
    }
}
