package com.abatra.billingr.google;

import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;

public class BillingClientFactory {

    private final Context context;

    public BillingClientFactory(Context context) {
        this.context = context;
    }

    BillingClient createPendingPurchasesEnabledBillingClient(PurchasesUpdatedListener purchasesUpdatedListener) {
        return BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();
    }
}
