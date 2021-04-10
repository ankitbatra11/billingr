package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ConsumeParams;

import timber.log.Timber;

import static com.abatra.billingr.google.GoogleBillingUtils.*;
import static com.abatra.billingr.google.GoogleBillingUtils.isOk;

public class GooglePurchaseConsumer implements PurchaseConsumer {

    private final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseConsumer(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void consumePurchase(SkuPurchase skuPurchase, Callback callback) {
        try {
            tryGettingInitializedBillingClient(skuPurchase, callback);
        } catch (Throwable error) {
            Timber.e(error);
            callback.onPurchaseConsumeFailed(new BillingrException(error));
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
                    tryConsumingPurchase(skuPurchase, callback, billingClient);
                } catch (Throwable error) {
                    Timber.e(error);
                    callback.onPurchaseConsumeFailed(new GoogleBillingrException(error));
                }
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onPurchaseConsumeFailed(new GoogleBillingrException(billingrException));
            }
        });
    }

    private void tryConsumingPurchase(SkuPurchase skuPurchase,
                                      Callback callback,
                                      BillingClient billingClient) {

        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(skuPurchase.getPurchaseToken())
                .build();

        billingClient.consumeAsync(consumeParams, (billingResult, s) -> {
            if (isOk(billingResult)) {
                Timber.i("purchase=%s has been consumed!", skuPurchase);
                callback.onPurchaseConsumed();
            } else {
                callback.onPurchaseConsumeFailed(reportErrorAndGet(billingResult, "Consuming purchase=%s failed!", skuPurchase));
            }
        });
    }
}
