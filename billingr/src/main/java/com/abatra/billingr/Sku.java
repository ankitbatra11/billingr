package com.abatra.billingr;

public class Sku {

    private SkuType type;
    private String title;
    private String currency;
    private long price;

    public SkuType getType() {
        return type;
    }

    public void setType(SkuType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
