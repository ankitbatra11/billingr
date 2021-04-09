package com.abatra.billingr.sku;

public enum SkuType {
    IN_APP_PRODUCT {
        @Override
        public String asPurchasableItemCategory() {
            return "in_app_product";
        }
    },
    SUBSCRIPTION {
        @Override
        public String asPurchasableItemCategory() {
            return "subscription";
        }
    };

    public abstract String asPurchasableItemCategory();
}
