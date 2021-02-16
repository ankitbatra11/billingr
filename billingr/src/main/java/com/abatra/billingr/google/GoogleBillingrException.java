package com.abatra.billingr.google;

import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingResult;

class GoogleBillingrException extends BillingrException {

    GoogleBillingrException(String message) {
        super(message);
    }

    static GoogleBillingrException from(BillingResult billingResult) {
        return new GoogleBillingrException(GoogleBillingUtils.toString(billingResult));
    }
}
