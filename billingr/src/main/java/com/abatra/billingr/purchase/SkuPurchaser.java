package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

public interface SkuPurchaser extends Observable<PurchaseListener>, ILifecycleObserver {

    void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest);

    interface Listener extends BillingUnavailableCallback {

        void onPurchaseFlowLaunchedSuccessfully();

        void onPurchaseFlowLaunchFailed(BillingrException billingrException);
    }
}
