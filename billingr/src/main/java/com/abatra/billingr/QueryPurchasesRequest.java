package com.abatra.billingr;

public class QueryPurchasesRequest {

    private SkuType skuType;
    private PurchaseListener purchaseListener;

    private QueryPurchasesRequest() {
    }

    public SkuType getSkuType() {
        return skuType;
    }

    public PurchaseListener getPurchaseListener() {
        return purchaseListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private SkuType skuType;
        private PurchaseListener purchaseListener;

        private Builder() {
        }

        public Builder setSkuType(SkuType skuType) {
            this.skuType = skuType;
            return this;
        }

        public Builder setPurchaseListener(PurchaseListener purchaseListener) {
            this.purchaseListener = purchaseListener;
            return this;
        }

        public QueryPurchasesRequest build() {
            QueryPurchasesRequest queryPurchasesRequest = new QueryPurchasesRequest();
            queryPurchasesRequest.skuType = skuType;
            queryPurchasesRequest.purchaseListener = purchaseListener;
            return queryPurchasesRequest;
        }
    }
}
