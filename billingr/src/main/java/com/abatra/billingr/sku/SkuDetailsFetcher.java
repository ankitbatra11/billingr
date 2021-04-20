package com.abatra.billingr.sku;

import com.abatra.android.wheelie.lifecycle.observer.ILifecycleObserver;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

import java.util.List;

public interface SkuDetailsFetcher extends ILifecycleObserver {

    void fetchInAppSkuDetails(List<String> skus, Listener listener);

    interface Listener extends BillingUnavailableCallback {

        void skusLoaded(List<Sku> skus);

        void loadingSkuDetailsFailed(BillingrException billingrException);
    }
}
