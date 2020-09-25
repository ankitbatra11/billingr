package com.abatra.billingr;

import com.android.billingclient.api.BillingClient;

public enum SkuType {
    IN_APP_PRODUCT(BillingClient.SkuType.INAPP),
    SUBSCRIPTION(BillingClient.SkuType.SUBS);

    private final String googleSkuType;

    SkuType(String googleSkuType) {
        this.googleSkuType = googleSkuType;
    }

    public String getGoogleSkuType() {
        return googleSkuType;
    }
}
