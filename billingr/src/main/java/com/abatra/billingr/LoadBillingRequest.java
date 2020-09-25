package com.abatra.billingr;

import java.util.Collection;
import java.util.HashSet;

public class LoadBillingRequest {

    private BillingAvailabilityListener billingAvailabilityListener;
    private boolean enablePendingPurchases;
    private final Collection<SkuType> acknowledgePurchasesSkuTypes = new HashSet<>();

    private LoadBillingRequest() {
    }

    public BillingAvailabilityListener getBillingAvailabilityListener() {
        return billingAvailabilityListener;
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

        private BillingAvailabilityListener billingAvailabilityListener;
        private boolean enablePendingPurchases = true;
        private final Collection<SkuType> acknowledgePurchasesSkuTypes = new HashSet<>();

        private Builder() {
        }

        public Builder setBillingAvailabilityListener(BillingAvailabilityListener billingAvailabilityListener) {
            this.billingAvailabilityListener = billingAvailabilityListener;
            return this;
        }

        public Builder acknowledgePurchases(SkuType skuType) {
            acknowledgePurchasesSkuTypes.add(skuType);
            return this;
        }

        public LoadBillingRequest build() {
            LoadBillingRequest loadBillingRequest = new LoadBillingRequest();
            loadBillingRequest.billingAvailabilityListener = billingAvailabilityListener;
            loadBillingRequest.enablePendingPurchases = enablePendingPurchases;
            loadBillingRequest.acknowledgePurchasesSkuTypes.addAll(acknowledgePurchasesSkuTypes);
            return loadBillingRequest;
        }
    }
}
