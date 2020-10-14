package com.abatra.billingr.exception;

class BillingrException extends RuntimeException {

    BillingrException(String message) {
        super(message);
    }

    BillingrException(Throwable cause) {
        super(cause);
    }
}
