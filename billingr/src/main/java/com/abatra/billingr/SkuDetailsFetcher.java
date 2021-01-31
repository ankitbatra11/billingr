package com.abatra.billingr;

import com.abatra.billingr.sku.Sku;

import java.util.List;

public interface SkuDetailsFetcher {

    void fetchInAppSkuDetails(List<String> skus, Listener listener);

    interface Listener {
        void loaded(List<Sku> skus);
    }
}
