package com.abatra.billingr.google;

import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingResult;

public class GoogleBillingrException extends BillingrException {

    public static final GoogleBillingrException UNAVAILABLE = new GoogleBillingrException("Billing is unavailable!");

    GoogleBillingrException(String message) {
        super(message);
    }

    GoogleBillingrException(Throwable cause) {
        super(cause);
    }

    static GoogleBillingrException from(BillingResult billingResult) {
        return new GoogleBillingrException(GoogleBillingUtils.toString(billingResult));
    }

    public static GoogleBillingrException unavailable() {
        return UNAVAILABLE;
    }
}
