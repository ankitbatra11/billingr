package com.abatra.billingr.sku;

import com.abatra.billingr.exception.LoadingSkuFailedException;

import java.util.List;

public interface SkuListener {

    default void onSkuLoaded(List<Sku> skus) {
    }

    default void onLoadingSkusFailed(LoadingSkuFailedException e) {
    }
}
