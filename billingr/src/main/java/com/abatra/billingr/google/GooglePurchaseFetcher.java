package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.ProcessLessPurchasesProcessor;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.PurchasesProcessor;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import timber.log.Timber;

import static com.android.billingclient.api.Purchase.PurchasesResult;

public class GooglePurchaseFetcher implements PurchaseFetcher {

    final InitializedBillingClientSupplier billingClientSupplier;
    private PurchasesProcessor purchasesProcessor = ProcessLessPurchasesProcessor.INSTANCE;

    public GooglePurchaseFetcher(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void setPurchasesProcessor(PurchasesProcessor purchasesProcessor) {
        this.purchasesProcessor = purchasesProcessor;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
        purchasesProcessor.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
        try {
            tryGettingInitializedBillingClient(listener);
        } catch (Throwable error) {
            Timber.e(error);
            listener.onPurchasesUpdateFailed(new BillingrException(error));
        }
    }

    private void tryGettingInitializedBillingClient(PurchaseListener listener) {
        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {
                queryPurchases(billingClient, listener);
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                listener.onPurchasesUpdateFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                listener.onBillingUnavailable();
            }
        });
    }

    private void queryPurchases(BillingClient billingClient, PurchaseListener listener) {
        PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (GoogleBillingUtils.isOk(purchasesResult.getBillingResult())) {
            List<Purchase> purchases = getPurchases(purchasesResult);
            if (purchases.isEmpty()) {
                listener.onPurchasesUpdated(Collections.emptyList());
            } else {
                processPurchases(listener, purchases);
            }
        } else {

            Timber.w("unexpected billing result=%s from inApp query purchases",
                    GoogleBillingUtils.toString(purchasesResult.getBillingResult()));

            listener.onPurchasesUpdateFailed(GoogleBillingrException.from(purchasesResult.getBillingResult()));
        }
    }

    private void processPurchases(PurchaseListener listener, List<Purchase> purchases) {
        List<SkuPurchase> skuPurchases = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(purchases.size());
        purchasesProcessor.processPurchases(GoogleBillingUtils.toSkuPurchases(purchases), new PurchasesProcessor.Listener() {

            @Override
            public void onProcessed(SkuPurchase skuPurchase) {
                try {
                    skuPurchases.add(skuPurchase);
                } finally {
                    countDownLatch.countDown();
                }
            }

            @Override
            public void onProcessingFailed(BillingrException billingrException) {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
            listener.onPurchasesUpdated(skuPurchases);
        } catch (InterruptedException e) {
            listener.onPurchasesUpdateFailed(new GoogleBillingrException(e));
        }
    }

    @NotNull
    private List<Purchase> getPurchases(PurchasesResult purchasesResult) {
        return Optional.ofNullable(purchasesResult.getPurchasesList()).orElse(Collections.emptyList());
    }
}
