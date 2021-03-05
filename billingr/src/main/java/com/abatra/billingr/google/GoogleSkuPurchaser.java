package com.abatra.billingr.google;

import android.app.Activity;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchaser;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.Sku;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;

import java.util.Locale;

import timber.log.Timber;

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
    public void removeObservers() {
        billingClientSupplier.removeObservers();
    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, Listener listener) {

        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {

                GoogleSku googleSku = (GoogleSku) sku;
                BillingResult billingResult = billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                        .setSkuDetails(googleSku.getSkuDetails())
                        .build());

                if (GoogleBillingUtils.isOk(billingResult)) {
                    listener.purchaseFlowLaunchedSuccessfully();
                } else {

                    String message = String.format(Locale.ENGLISH,
                            "Unexpected billing result=%s from launchBillingFlow",
                            GoogleBillingUtils.toString(billingResult));

                    if (GoogleBillingUtils.isError(billingResult)) {
                        Timber.e(new RuntimeException(message));
                    } else {
                        Timber.w(message);
                    }
                    listener.purchaseFlowLaunchFailed(GoogleBillingrException.from(billingResult));
                }
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                Timber.e(billingrException);
                listener.purchaseFlowLaunchFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                Timber.w("onBillingUnavailable");
                listener.onBillingUnavailable();
            }
        });
    }
}
