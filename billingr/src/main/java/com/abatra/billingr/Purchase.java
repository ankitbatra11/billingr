package com.abatra.billingr;

public class Purchase {

    private final String sku;
    private final String transactionId;

    public Purchase(String sku, String transactionId) {
        this.sku = sku;
        this.transactionId = transactionId;
    }

    public String getSku() {
        return sku;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
