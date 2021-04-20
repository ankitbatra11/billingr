package com.abatra.billingr.purchase;

import com.abatra.android.wheelie.lifecycle.observer.ILifecycleObserver;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.PurchasesNotifier;

public interface SkuPurchaser extends PurchasesNotifier, ILifecycleObserver {

    void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest);

    interface Listener extends BillingUnavailableCallback {

        void onPurchaseFlowLaunchedSuccessfully();

        void onPurchaseFlowLaunchFailed(BillingrException billingrException);
    }
}
