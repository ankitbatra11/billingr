package com.abatra.billingr;

import com.abatra.billingr.exception.BillingrException;
import com.abatra.billingr.sku.Sku;

import java.util.List;

public interface SkuDetailsFetcher {

    void fetchInAppSkuDetails(List<String> skus, Listener listener);

    interface Listener {

        default void skusLoaded(List<Sku> skus) {
        }

        default void loadingSkuDetailsFailed(BillingrException billingrException) {
        }

    }
}
