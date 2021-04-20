package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.owner.ILifecycleOwner;
import com.abatra.billingr.BillingAvailabilityChecker;
import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingClient;

public class GoogleBillingAvailabilityChecker implements BillingAvailabilityChecker {

    final InitializedBillingClientSupplier billingClientSupplier;

    public GoogleBillingAvailabilityChecker(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void checkBillingAvailability(Callback callback) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void onBillingUnavailable() {
                callback.onBillingUnavailable();
            }

            @Override
            public void initialized(BillingClient billingClient) {
                callback.onBillingAvailable(billingClient);
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onBillingAvailabilityCheckFailed(billingrException);
            }
        });
    }
}
