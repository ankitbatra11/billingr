package com.abatra.billingr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class LoadBillingRequest {

    private LoadBillingListener loadBillingListener;
    private boolean enablePendingPurchases;
    private final Collection<SkuType> acknowledgePurchasesSkuTypes = new HashSet<>();
    private QueryPurchasesRequest queryPurchasesRequest;
    private PurchaseListener purchaseListener;

    private LoadBillingRequest() {
    }

    public LoadBillingListener getLoadBillingListener() {
        return loadBillingListener;
    }

    public boolean isEnablePendingPurchases() {
        return enablePendingPurchases;
    }

    public Collection<SkuType> getAcknowledgePurchasesSkuTypes() {
        return acknowledgePurchasesSkuTypes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public QueryPurchasesRequest getQueryPurchasesRequest() {
        return queryPurchasesRequest;
    }

    public PurchaseListener getPurchaseListener() {
        return purchaseListener;
    }

    public static class Builder {

        private LoadBillingListener loadBillingListener;
        private boolean enablePendingPurchases = true;
        private final Collection<SkuType> acknowledgePurchasesSkuTypes = new HashSet<>();
        private QueryPurchasesRequest queryPurchasesRequest;
        private PurchaseListener purchaseListener;

        private Builder() {
        }

        public Builder setLoadBillingListener(LoadBillingListener loadBillingListener) {
            this.loadBillingListener = loadBillingListener;
            return this;
        }

        public Builder acknowledgePurchases(SkuType... skuType) {
            acknowledgePurchasesSkuTypes.addAll(Arrays.asList(skuType));
            return this;
        }

        public Builder setQueryPurchasesRequest(QueryPurchasesRequest queryPurchasesRequest) {
            this.queryPurchasesRequest = queryPurchasesRequest;
            return this;
        }

        public Builder setPurchaseListener(PurchaseListener purchaseListener) {
            this.purchaseListener = purchaseListener;
            return this;
        }

        public LoadBillingRequest build() {
            LoadBillingRequest loadBillingRequest = new LoadBillingRequest();
            loadBillingRequest.loadBillingListener = loadBillingListener;
            loadBillingRequest.enablePendingPurchases = enablePendingPurchases;
            loadBillingRequest.acknowledgePurchasesSkuTypes.addAll(acknowledgePurchasesSkuTypes);
            loadBillingRequest.queryPurchasesRequest = queryPurchasesRequest;
            loadBillingRequest.purchaseListener = purchaseListener;
            return loadBillingRequest;
        }
    }
}
