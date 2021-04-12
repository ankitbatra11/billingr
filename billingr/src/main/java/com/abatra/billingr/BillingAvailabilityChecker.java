package com.abatra.billingr;

import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.android.billingclient.api.BillingClient;

public interface BillingAvailabilityChecker extends ILifecycleObserver {

    void checkBillingAvailability(Callback callback);

    interface Callback extends BillingUnavailableCallback {

        void onBillingAvailable(BillingClient billingClient);

        void onBillingAvailabilityCheckFailed(BillingrException billingrException);
    }
}
