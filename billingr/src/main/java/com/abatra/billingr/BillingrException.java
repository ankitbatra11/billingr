package com.abatra.billingr;

public class BillingrException extends RuntimeException {

    public static final BillingrException UNAVAILABLE = new BillingrException("Billing is unavailable!");

    public BillingrException(String message) {
        super(message);
    }

    public BillingrException(Throwable cause) {
        super(cause);
    }

    public static BillingrException unavailable() {
        return UNAVAILABLE;
    }
}
