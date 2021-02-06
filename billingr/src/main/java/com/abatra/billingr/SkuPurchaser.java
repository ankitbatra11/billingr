package com.abatra.billingr;

import android.app.Activity;

import com.abatra.android.wheelie.pattern.Observable;

public interface SkuPurchaser extends Observable<PurchaseListener> {

    void launchPurchaseFlow(Sku sku, Activity activity, Listener listener);

    interface Listener {

        default void purchaseFlowLaunchedSuccessfully() {
        }

        default void purchaseFlowLaunchFailed(BillingrException billingrException) {
        }
    }
}
