package com.abatra.billingr;

public class QueryPurchasesRequest {

    private SkuType skuType;
    private boolean acknowledgePurchases;
    private PurchaseListener purchaseListener;

    private QueryPurchasesRequest() {
    }

    public SkuType getSkuType() {
        return skuType;
    }

    public boolean isAcknowledgePurchases() {
        return acknowledgePurchases;
    }

    public PurchaseListener getPurchaseListener() {
        return purchaseListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static class Builder {

        private SkuType skuType;
        private boolean acknowledgePurchases = true;
        private PurchaseListener purchaseListener;

        private Builder() {
        }

        public void setSkuType(SkuType skuType) {
            this.skuType = skuType;
        }

        public Builder setAcknowledgePurchases(boolean acknowledgePurchases) {
            this.acknowledgePurchases = acknowledgePurchases;
            return this;
        }

        public Builder setPurchaseListener(PurchaseListener purchaseListener) {
            this.purchaseListener = purchaseListener;
            return this;
        }

        public QueryPurchasesRequest build() {
            QueryPurchasesRequest queryPurchasesRequest = new QueryPurchasesRequest();
            queryPurchasesRequest.skuType = skuType;
            queryPurchasesRequest.acknowledgePurchases = acknowledgePurchases;
            queryPurchasesRequest.purchaseListener = purchaseListener;
            return queryPurchasesRequest;
        }
    }
}
