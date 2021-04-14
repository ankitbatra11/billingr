package com.abatra.billingr.google;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.PurchaseSkuRequest;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;

import java.util.Locale;

import timber.log.Timber;

public class GoogleSkuPurchaser implements SkuPurchaser {

    final InitializedBillingClientSupplier billingClientSupplier;

    public GoogleSkuPurchaser(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
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
    public void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest) {
        try {
            tryGettingInitializedBillingClient(purchaseSkuRequest);
        } catch (Throwable error) {
            Timber.e(error);
            onPurchaseFlowLaunchFailed(purchaseSkuRequest, error);
        }
    }

    private void onPurchaseFlowLaunchFailed(PurchaseSkuRequest purchaseSkuRequest, Throwable error) {
        purchaseSkuRequest.getListener().ifPresent(l -> l.onPurchaseFlowLaunchFailed(new GoogleBillingrException(error)));
    }

    private void tryGettingInitializedBillingClient(PurchaseSkuRequest purchaseSkuRequest) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {
                try {
                    launchBillingFlow(billingClient, purchaseSkuRequest);
                } catch (Throwable error) {
                    Timber.e(error);
                    onPurchaseFlowLaunchFailed(purchaseSkuRequest, error);
                }
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                Timber.e(billingrException);
                onPurchaseFlowLaunchFailed(purchaseSkuRequest, billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                Timber.w("onBillingUnavailable");
                purchaseSkuRequest.getListener().ifPresent(Listener::onBillingUnavailable);
            }
        });
    }

    private void launchBillingFlow(BillingClient billingClient, PurchaseSkuRequest purchaseSkuRequest) {

        GoogleSku googleSku = (GoogleSku) purchaseSkuRequest.getSku();

        BillingResult billingResult = billingClient.launchBillingFlow(purchaseSkuRequest.getActivity(), BillingFlowParams.newBuilder()
                .setSkuDetails(googleSku.getSkuDetails())
                .build());

        if (GoogleBillingUtils.isOk(billingResult)) {
            purchaseSkuRequest.getListener().ifPresent(Listener::onPurchaseFlowLaunchedSuccessfully);
        } else {
            onLaunchBillingFlowFailure(purchaseSkuRequest, billingResult);
        }
    }

    private void onLaunchBillingFlowFailure(PurchaseSkuRequest purchaseSkuRequest, BillingResult billingResult) {

        String message = String.format(Locale.ENGLISH,
                "Unexpected billing result=%s from launchBillingFlow",
                GoogleBillingUtils.toString(billingResult));

        if (GoogleBillingUtils.isError(billingResult)) {
            Timber.e(new RuntimeException(message));
        } else {
            Timber.w(message);
        }
        purchaseSkuRequest.getListener().ifPresent(l -> {
            GoogleBillingrException billingrException = GoogleBillingrException.from(billingResult);
            onPurchaseFlowLaunchFailed(purchaseSkuRequest, billingrException);
        });
    }
}
