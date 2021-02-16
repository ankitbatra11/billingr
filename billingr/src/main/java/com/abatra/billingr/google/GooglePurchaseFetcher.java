package com.abatra.billingr.google;

import com.abatra.billingr.PurchaseFetcher;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GooglePurchaseFetcher implements PurchaseFetcher {

    private final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseFetcher(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {
                Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                if (GoogleBillingUtils.isOk(purchasesResult.getBillingResult())) {
                    List<SkuPurchase> skuPurchases = new ArrayList<>();
                    if (purchasesResult.getPurchasesList() != null) {
                        for (Purchase purchase : purchasesResult.getPurchasesList()) {
                            if (!purchase.isAcknowledged()) {
                                try {
                                    tryAcknowledgingPurchase(billingClient, purchase);
                                } catch (Exception e) {
                                    Timber.e(e, "tryAcknowledgingPurchase failed for sku=%s", purchase.getSku());
                                }
                            }
                            if (GoogleBillingUtils.isPurchased(purchase)) {
                                skuPurchases.add(new GoogleSkuPurchase(purchase));
                            }
                        }
                    }
                    listener.updated(skuPurchases);
                } else {

                    Timber.w("unexpected billing result=%s from inApp query purchases",
                            GoogleBillingUtils.toString(purchasesResult.getBillingResult()));

                    listener.loadingPurchasesFailed(GoogleBillingrException.from(purchasesResult.getBillingResult()));
                }
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                listener.loadingPurchasesFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                listener.onBillingUnavailable();
            }
        });
    }

    private void tryAcknowledgingPurchase(BillingClient billingClient, Purchase purchase) {

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
            if (!GoogleBillingUtils.isOk(result)) {
                Timber.w("unexpected billing result=%s from acknowledgePurchase for sku=%s",
                        GoogleBillingUtils.toString(result), purchase.getSku());
            }
        });
    }
}
