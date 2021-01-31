package com.abatra.billingr.google;

import com.abatra.billingr.PurchaseAcknowledger;
import com.abatra.billingr.PurchaseFetcher;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.util.BillingUtils;
import com.android.billingclient.api.AcknowledgePurchaseParams;

import timber.log.Timber;

public class GooglePurchaseAcknowledger implements PurchaseAcknowledger {

    private final PurchaseFetcher purchaseFetcher;
    private final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseAcknowledger(PurchaseFetcher purchaseFetcher,
                                      InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
        this.purchaseFetcher = purchaseFetcher;
    }

    @Override
    public void acknowledgeInAppPurchases(Listener listener) {
        purchaseFetcher.fetchUnacknowledgedInAppPurchases(skuPurchases -> {
            for (SkuPurchase skuPurchase : skuPurchases) {
                try {
                    tryAcknowledgingPurchase(skuPurchase, listener);
                } catch (Exception e) {
                    Timber.e(e, "tryAcknowledgingPurchase failed for sku=%s", skuPurchase.getSku());
                }
            }
        });
    }

    private void tryAcknowledgingPurchase(SkuPurchase skuPurchase, Listener listener) {
        billingClientSupplier.getInitializedBillingClient(billingClient -> {

            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(skuPurchase.getPurchaseToken())
                    .build();

            billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
                if (BillingUtils.isOk(result)) {
                    listener.purchaseAcknowledged(skuPurchase.getSku());
                } else {
                    Timber.w("unexpected billing result=%s from acknowledgePurchase for sku=%s",
                            BillingUtils.toString(result), skuPurchase.getSku());
                }
            });
        });


    }
}
