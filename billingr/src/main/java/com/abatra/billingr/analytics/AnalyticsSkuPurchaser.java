package com.abatra.billingr.analytics;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.abatra.android.wheelie.chronicle.BeginCheckoutEventParams;
import com.abatra.android.wheelie.chronicle.Chronicle;
import com.abatra.android.wheelie.chronicle.PurchaseEventParams;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.SkuPurchaser;

import java.util.List;
import java.util.Optional;

abstract public class AnalyticsSkuPurchaser implements SkuPurchaser, PurchaseListener {

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
        skuPurchases.stream()
                .filter(skuPurchase -> skuPurchase.getSku().equalsIgnoreCase(sku.getId()))
                .findFirst()
                .ifPresent(skuPurchase -> Chronicle.recordPurchaseEvent(createPurchaseEventParams(sku, skuPurchase)));
    }

    protected abstract PurchaseEventParams createPurchaseEventParams(Sku sku, SkuPurchase skuPurchase);

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
        Chronicle.recordBeginCheckoutEvent(createBeginCheckoutEventParams(checkedOutSku));
    }

    protected abstract BeginCheckoutEventParams createBeginCheckoutEventParams(Sku checkedOutSku);

    /* Testing */
    void setCheckedOutSku(@Nullable Sku checkedOutSku) {
        this.checkedOutSku = checkedOutSku;
    }
}
