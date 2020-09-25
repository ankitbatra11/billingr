package com.abatra.billingr;

import java.util.List;

public interface SkuListener {
    void onSkuLoaded(List<Sku> skus);
}
