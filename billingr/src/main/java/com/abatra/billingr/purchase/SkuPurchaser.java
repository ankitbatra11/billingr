package com.abatra.billingr.purchase;

import android.app.Activity;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.sku.Sku;

public interface SkuPurchaser extends Observable<PurchaseListener>, ILifecycleObserver {

    /**
     * @param sku      to purchase
     * @param activity to use to launch purchase flow
     * @param listener to receive callbacks
     * @deprecated Use {@link #launchPurchaseFlow(PurchaseSkuRequest)} instead.
     */
    default void launchPurchaseFlow(Sku sku, Activity activity, Listener listener) {
        launchPurchaseFlow(new PurchaseSkuRequest(activity, sku).setListener(listener));
    }

    void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest);

    interface Listener extends BillingUnavailableCallback {

        void onPurchaseFlowLaunchedSuccessfully();

        void onPurchaseFlowLaunchFailed(BillingrException billingrException);
    }
}
