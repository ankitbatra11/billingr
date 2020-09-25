package com.abatra.billingr;

import java.util.Collection;
import java.util.HashSet;

public class LoadBillingRequest {

    private LoadBillingListener loadBillingListener;
    private boolean enablePendingPurchases;
    private final Collection<SkuType> acknowledgePurchasesSkuTypes = new HashSet<>();

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

    public static class Builder {

        private LoadBillingListener loadBillingListener;
        private boolean enablePendingPurchases = true;
        private final Collection<SkuType> acknowledgePurchasesSkuTypes = new HashSet<>();

        private Builder() {
        }

        public Builder setLoadBillingListener(LoadBillingListener loadBillingListener) {
            this.loadBillingListener = loadBillingListener;
            return this;
        }

        public Builder acknowledgePurchases(SkuType skuType) {
            acknowledgePurchasesSkuTypes.add(skuType);
            return this;
        }

        public LoadBillingRequest build() {
            LoadBillingRequest loadBillingRequest = new LoadBillingRequest();
            loadBillingRequest.loadBillingListener = loadBillingListener;
            loadBillingRequest.enablePendingPurchases = enablePendingPurchases;
            loadBillingRequest.acknowledgePurchasesSkuTypes.addAll(acknowledgePurchasesSkuTypes);
            return loadBillingRequest;
        }
    }
}
