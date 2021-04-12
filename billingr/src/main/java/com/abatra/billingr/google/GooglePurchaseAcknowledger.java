package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;

import timber.log.Timber;

public class GooglePurchaseAcknowledger implements PurchaseAcknowledger {

    final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseAcknowledger(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void acknowledgePurchase(SkuPurchase skuPurchase, Callback callback) {
        try {
            tryGettingInitializedBillingClient(skuPurchase, callback);
        } catch (Throwable error) {
            Timber.e(error);
            callback.onPurchaseAcknowledgeFailed(new BillingrException(error));
        }
    }

    private void tryGettingInitializedBillingClient(SkuPurchase skuPurchase, Callback callback) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void onBillingUnavailable() {
                callback.onBillingUnavailable();
            }

            @Override
            public void initialized(BillingClient billingClient) {
                try {
                    tryAcknowledgingPurchase(skuPurchase, callback, billingClient);
                } catch (Throwable error) {
                    Timber.e(error);
                    callback.onPurchaseAcknowledgeFailed(new GoogleBillingrException(error));
                }
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onPurchaseAcknowledgeFailed(billingrException);
            }
        });
    }

    private void tryAcknowledgingPurchase(SkuPurchase skuPurchase, Callback callback, BillingClient billingClient) {
        GoogleSkuPurchase googleSkuPurchase = (GoogleSkuPurchase) skuPurchase;
        if (googleSkuPurchase.getPurchase().isAcknowledged()) {
            callback.onPurchaseAcknowledged();
        } else {
            if (GoogleBillingUtils.isPurchased(googleSkuPurchase.getPurchase())) {
                acknowledgePurchase(googleSkuPurchase, callback, billingClient);
            } else {
                String message = "Purchase=" + skuPurchase + " has not been purchased yet!";
                Timber.w(message);
                callback.onPurchaseAcknowledgeFailed(new GoogleBillingrException(message));
            }
        }
    }

    private void acknowledgePurchase(GoogleSkuPurchase googleSkuPurchase, Callback callback, BillingClient billingClient) {

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(googleSkuPurchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
            if (GoogleBillingUtils.isOk(result)) {
                Timber.i("purchase=%s acknowledged successfully!", googleSkuPurchase);
                callback.onPurchaseAcknowledged();
            } else {

                Timber.w("unexpected billing result=%s from acknowledgePurchase for sku=%s",
                        GoogleBillingUtils.toString(result), googleSkuPurchase.getSku());

                callback.onPurchaseAcknowledgeFailed(GoogleBillingrException.from(result));
            }
        });
    }
}
