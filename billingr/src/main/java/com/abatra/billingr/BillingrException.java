package com.abatra.billingr;

abstract public class BillingrException extends RuntimeException {

    public BillingrException(String message) {
        super(message);
    }

    public BillingrException(Throwable cause) {
        super(cause);
    }
}
