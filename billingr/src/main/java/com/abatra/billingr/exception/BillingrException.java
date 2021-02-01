package com.abatra.billingr.exception;

import com.abatra.billingr.util.BillingUtils;
import com.android.billingclient.api.BillingResult;

public class BillingrException extends RuntimeException {

    public BillingrException(String message) {
        super(message);
    }

    public static BillingrException from(BillingResult billingResult) {
        return new BillingrException(BillingUtils.toString(billingResult));
    }

    BillingrException(Throwable cause) {
        super(cause);
    }
}
