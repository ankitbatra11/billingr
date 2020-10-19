package com.abatra.billingr.load;

import androidx.annotation.Nullable;

import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.QueryPurchasesRequest;

import java.lang.ref.WeakReference;

import static com.abatra.billingr.utils.WeakReferenceUtils.createWeakReference;

public class LoadBillingRequest {

    private WeakReference<LoadBillingListener> loadBillingListener;
    private boolean enablePendingPurchases;
    private QueryPurchasesRequest queryPurchasesRequest;
    private WeakReference<PurchaseListener> purchaseListener;

    private LoadBillingRequest() {
    }

    @Nullable
    public LoadBillingListener getLoadBillingListener() {
        return loadBillingListener.get();
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
        return purchaseListener.get();
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
            loadBillingRequest.loadBillingListener = createWeakReference(loadBillingListener);
            loadBillingRequest.enablePendingPurchases = enablePendingPurchases;
            loadBillingRequest.queryPurchasesRequest = queryPurchasesRequest;
            loadBillingRequest.purchaseListener = createWeakReference(purchaseListener);
            return loadBillingRequest;
        }
    }
}
