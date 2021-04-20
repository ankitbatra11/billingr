package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.owner.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import timber.log.Timber;

import static com.abatra.billingr.google.GoogleBillingUtils.isOk;
import static com.abatra.billingr.google.GoogleBillingUtils.reportErrorAndGet;
import static com.android.billingclient.api.Purchase.PurchasesResult;

public class GooglePurchaseFetcher implements PurchaseFetcher {

    final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseFetcher(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
        try {
            tryGettingInitializedBillingClient(listener);
        } catch (Throwable error) {
            Timber.e(error);
            listener.onPurchasesLoadFailed(new GoogleBillingrException(error));
        }
    }

    private void tryGettingInitializedBillingClient(PurchaseListener listener) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {
                try {
                    queryPurchases(billingClient, listener);
                } catch (Throwable error) {
                    Timber.e(error);
                    listener.onPurchasesLoadFailed(new GoogleBillingrException(error));
                }
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                listener.onPurchasesLoadFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                listener.onBillingUnavailable();
            }
        });
    }

    private void queryPurchases(BillingClient billingClient, PurchaseListener listener) {
        PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (isOk(purchasesResult.getBillingResult())) {
            listener.onPurchasesLoaded(toSkuPurchases(purchasesResult));
        } else {
            listener.onPurchasesLoadFailed(reportErrorAndGet(purchasesResult.getBillingResult(),
                    "queryPurchases(inApp) failed!"));
        }
    }

    private List<SkuPurchase> toSkuPurchases(PurchasesResult purchasesResult) {

        List<Purchase> purchases = Optional.ofNullable(purchasesResult.getPurchasesList())
                .orElse(Collections.emptyList());

        return GoogleBillingUtils.toSkuPurchases(purchases);
    }
}
