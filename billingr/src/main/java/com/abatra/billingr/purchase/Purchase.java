package com.abatra.billingr.purchase;

public class Purchase {

    private String sku;
    private String transactionId;

    public Purchase(String sku, String transactionId) {
        this.sku = sku;
        this.transactionId = transactionId;
    }

    public Purchase() {
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSku() {
        return sku;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
