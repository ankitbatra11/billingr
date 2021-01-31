package com.abatra.billingr;

import android.app.Activity;

import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.sku.Sku;

public interface SkuPurchaser extends Observable<PurchaseListener> {

    void launchPurchaseFlow(Sku sku, Activity activity, Listener listener);

    interface Listener {
        void loaded(LaunchPurchaseFlowResult launchPurchaseFlowResult);
    }
}
