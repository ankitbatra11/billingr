package com.abatra.billingr.google;

import android.app.Activity;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.billingr.GoogleLaunchPurchaseFlowResult;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchaser;
import com.abatra.billingr.sku.Sku;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;

public class GoogleSkuPurchaser implements SkuPurchaser {

    private final InitializedBillingClientSupplier billingClientSupplier;

    public GoogleSkuPurchaser(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void addObserver(PurchaseListener observer) {
        billingClientSupplier.addObserver(observer);
    }

    @Override
    public void removeObserver(PurchaseListener observer) {
        billingClientSupplier.removeObserver(observer);
    }

    @Override
    public void forEachObserver(Consumer<PurchaseListener> observerConsumer) {
        billingClientSupplier.forEachObserver(observerConsumer);
    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, Listener listener) {
        billingClientSupplier.getInitializedBillingClient(billingClient -> {

            GoogleSku googleSku = (GoogleSku) sku;
            BillingResult billingResult = billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                    .setSkuDetails(googleSku.getSkuDetails())
                    .build());

            listener.loaded(new GoogleLaunchPurchaseFlowResult(billingResult));
        });
    }
}
