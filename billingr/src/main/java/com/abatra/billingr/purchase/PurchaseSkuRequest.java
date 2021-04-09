package com.abatra.billingr.purchase;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.abatra.billingr.sku.Sku;

import java.util.Optional;

public class PurchaseSkuRequest {

    private final Activity activity;
    private final Sku sku;
    @Nullable
    private SkuPurchaser.Listener listener;

    public PurchaseSkuRequest(Activity activity, Sku sku) {
        this.activity = activity;
        this.sku = sku;
        this.listener = listener;
    }

    public PurchaseSkuRequest(PurchaseSkuRequest request) {
        this(request.getActivity(), request.getSku());
        request.getListener().ifPresent(this::setListener);
    }

    public PurchaseSkuRequest setListener(@Nullable SkuPurchaser.Listener listener) {
        this.listener = listener;
        return this;
    }

    public Optional<SkuPurchaser.Listener> getListener() {
        return Optional.ofNullable(listener);
    }

    public Activity getActivity() {
        return activity;
    }

    public Sku getSku() {
        return sku;
    }
}
