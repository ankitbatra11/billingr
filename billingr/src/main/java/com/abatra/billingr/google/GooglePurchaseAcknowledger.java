package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.owner.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.AcknowledgePurchaseCallback;
import com.abatra.billingr.purchase.AcknowledgePurchasesCallback;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;

import java.util.List;

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
    public void acknowledgePurchase(SkuPurchase skuPurchase, AcknowledgePurchaseCallback callback) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void onBillingUnavailable() {
                callback.onBillingUnavailable();
            }

            @Override
            public void initialized(BillingClient billingClient) {
                GooglePurchaseAcknowledger.this.acknowledgePurchase(billingClient, skuPurchase, callback);
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onPurchaseAcknowledgeFailed(skuPurchase, billingrException);
            }
        });
    }

    private void acknowledgePurchase(BillingClient billingClient, SkuPurchase skuPurchase, AcknowledgePurchaseCallback callback) {
        try {
            tryAcknowledgingPurchase(skuPurchase, callback, billingClient);
        } catch (Throwable error) {
            Timber.e(error);
            callback.onPurchaseAcknowledgeFailed(skuPurchase, new GoogleBillingrException(error));
        }
    }

    private void tryAcknowledgingPurchase(SkuPurchase skuPurchase, AcknowledgePurchaseCallback callback, BillingClient billingClient) {
        GoogleSkuPurchase googleSkuPurchase = (GoogleSkuPurchase) skuPurchase;
        if (skuPurchase.isAcknowledged()) {
            callback.onPurchaseAcknowledged(skuPurchase);
        } else {
            if (skuPurchase.isPurchased()) {
                acknowledgePurchase(googleSkuPurchase, callback, billingClient);
            } else {
                String message = "Purchase=" + skuPurchase + " has not been purchased yet!";
                GoogleBillingrException billingrException = new GoogleBillingrException(message);
                Timber.w(billingrException, message);
                callback.onPurchaseAcknowledgeFailed(skuPurchase, billingrException);
            }
        }
    }

    private void acknowledgePurchase(GoogleSkuPurchase googleSkuPurchase,
                                     AcknowledgePurchaseCallback callback,
                                     BillingClient billingClient) {

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(googleSkuPurchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
            if (GoogleBillingUtils.isOk(result)) {
                Timber.v("purchase=%s acknowledged successfully!", googleSkuPurchase);
                callback.onPurchaseAcknowledged(googleSkuPurchase);
            } else {
                GoogleBillingrException billingrException = GoogleBillingrException.from(result);
                Timber.w(billingrException, "Failed to acknowledge purchase for sku=%s", googleSkuPurchase.getSku());
                callback.onPurchaseAcknowledgeFailed(googleSkuPurchase, billingrException);
            }
        });
    }

    @Override
    public void acknowledgePurchases(List<SkuPurchase> skuPurchases, AcknowledgePurchasesCallback callback) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {
            @Override
            public void initialized(BillingClient billingClient) {
                acknowledgePurchases(billingClient, skuPurchases, callback);
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onPurchasesAcknowledgeFailure(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                callback.onBillingUnavailable();
            }
        });
    }

    private void acknowledgePurchases(BillingClient billingClient, List<SkuPurchase> skuPurchases, AcknowledgePurchasesCallback callback) {
        for (SkuPurchase skuPurchase : skuPurchases) {
            acknowledgePurchase(billingClient, skuPurchase, new AcknowledgePurchaseCallback() {
                @Override
                public void onPurchaseAcknowledged(SkuPurchase skuPurchase) {
                    callback.onPurchaseAcknowledged(skuPurchase);
                }

                @Override
                public void onPurchaseAcknowledgeFailed(SkuPurchase skuPurchase, BillingrException billingrException) {
                    callback.onPurchaseAcknowledgeFailed(skuPurchase, billingrException);
                }

                @Override
                public void onBillingUnavailable() {
                    callback.onBillingUnavailable();
                }
            });
        }
    }
}
