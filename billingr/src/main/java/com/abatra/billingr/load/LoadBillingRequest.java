package com.abatra.billingr.load;

import androidx.annotation.Nullable;

import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.QueryPurchasesRequest;

public class LoadBillingRequest {

    private LoadBillingListener loadBillingListener;
    private boolean enablePendingPurchases;
    private QueryPurchasesRequest queryPurchasesRequest;
    private PurchaseListener purchaseListener;

    private LoadBillingRequest() {
    }

    @Nullable
    public LoadBillingListener getLoadBillingListener() {
        return loadBillingListener;
    }

    public boolean isEnablePendingPurchases() {
        return enablePendingPurchases;
    }

    public static Builder builder() {
        return new Builder();
    }

    public QueryPurchasesRequest getQueryPurchasesRequest() {
        return queryPurchasesRequest;
    }

    @Nullable
    public PurchaseListener getPurchaseListener() {
        return purchaseListener;
    }

    public static class Builder {

        private LoadBillingListener loadBillingListener;
        private boolean enablePendingPurchases = true;
        private QueryPurchasesRequest queryPurchasesRequest;
        private PurchaseListener purchaseListener;

        private Builder() {
        }

        public Builder setLoadBillingListener(LoadBillingListener loadBillingListener) {
            this.loadBillingListener = loadBillingListener;
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
            loadBillingRequest.queryPurchasesRequest = queryPurchasesRequest;
            loadBillingRequest.purchaseListener = purchaseListener;
            return loadBillingRequest;
        }
    }
}
