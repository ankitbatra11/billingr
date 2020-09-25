package com.abatra.billingr;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.abatra.billingr.google.GoogleBillingUseCase;

public interface BillingUseCase extends LifecycleObserver {

    static BillingUseCase google(Context context) {
        return new GoogleBillingUseCase(context);
    }

    void loadBilling(LoadBillingRequest loadBillingRequest);

    void querySkus(QuerySkuRequest querySkuRequest);

    void queryPurchases(QueryPurchasesRequest queryPurchasesRequest);

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void destroy();
}
