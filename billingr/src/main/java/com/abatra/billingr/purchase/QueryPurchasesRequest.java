package com.abatra.billingr.purchase;

import androidx.annotation.NonNull;

import com.abatra.billingr.sku.SkuType;

public class QueryPurchasesRequest {

    private SkuType skuType;
    private boolean acknowledgePurchases;

    private QueryPurchasesRequest() {
    }

    public SkuType getSkuType() {
        return skuType;
    }
    public boolean isAcknowledgePurchases() {
        return acknowledgePurchases;
    }

    @NonNull
    @Override
    public String toString() {
        return "QueryPurchasesRequest{" +
                "skuType=" + skuType +
                ", acknowledgePurchases=" + acknowledgePurchases +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private SkuType skuType;
        private boolean acknowledgePurchases = true;

        private Builder() {
        }

        public Builder setSkuType(SkuType skuType) {
            this.skuType = skuType;
            return this;
        }

        public QueryPurchasesRequest build() {
            QueryPurchasesRequest queryPurchasesRequest = new QueryPurchasesRequest();
            queryPurchasesRequest.skuType = skuType;
            queryPurchasesRequest.acknowledgePurchases = acknowledgePurchases;
            return queryPurchasesRequest;
        }
    }
}
