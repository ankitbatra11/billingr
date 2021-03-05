package com.abatra.billingr;

import android.app.Activity;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public class AnalyticsSkuPurchaser implements SkuPurchaser, PurchaseListener {

    private final SkuPurchaser delegate;
    @Nullable
    private Sku checkedOutSku;

    public AnalyticsSkuPurchaser(SkuPurchaser delegate) {
        this.delegate = delegate;
        this.delegate.addObserver(this);
    }

    @Override
    public void updated(List<SkuPurchase> skuPurchases) {
        Optional.ofNullable(checkedOutSku).ifPresent(sku -> logPurchaseEvent(sku, skuPurchases));
    }

    private void logPurchaseEvent(Sku sku, List<SkuPurchase> skuPurchases) {
        // TODO bring chronicle to wheelie
    }

    @Override
    public void addObserver(PurchaseListener observer) {
        delegate.addObserver(observer);
    }

    @Override
    public void removeObserver(PurchaseListener observer) {
        delegate.removeObserver(observer);
    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, Listener listener) {
        delegate.launchPurchaseFlow(sku, activity, new Listener() {

            @Override
            public void onBillingUnavailable() {
                listener.onBillingUnavailable();
            }

            @Override
            public void purchaseFlowLaunchedSuccessfully() {
                checkedOutSku = sku;
                logBeginCheckoutEvent(checkedOutSku);
                listener.purchaseFlowLaunchedSuccessfully();
            }

            @Override
            public void purchaseFlowLaunchFailed(BillingrException billingrException) {
                listener.purchaseFlowLaunchFailed(billingrException);
            }
        });
    }

    private void logBeginCheckoutEvent(Sku checkedOutSku) {
        // TODO: bring chronicle to wheelie
    }
}
