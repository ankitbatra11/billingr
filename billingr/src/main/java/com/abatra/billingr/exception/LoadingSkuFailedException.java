package com.abatra.billingr.exception;

public class LoadingSkuFailedException extends BillingrException {

    public LoadingSkuFailedException(String message) {
        super(message);
    }

    public LoadingSkuFailedException(Throwable cause) {
        super(cause);
    }
}
