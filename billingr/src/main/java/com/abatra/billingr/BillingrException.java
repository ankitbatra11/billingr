package com.abatra.billingr;

public class BillingrException extends RuntimeException {

    public BillingrException(String message) {
        super(message);
    }

    public BillingrException(Throwable cause) {
        super(cause);
    }

    public static BillingrException unavailable() {
        return new BillingrException("Billing is unavailable!");
    }
}
