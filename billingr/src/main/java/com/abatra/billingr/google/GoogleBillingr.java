package com.abatra.billingr.google;

import android.app.Activity;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingAvailabilityChecker;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.PurchaseSkuRequest;
import com.abatra.billingr.purchase.PurchasesProcessor;
import com.abatra.billingr.sku.SkuDetailsFetcher;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.sku.Sku;

import java.util.List;

public class GoogleBillingr implements Billingr {

    private final InitializedBillingClientSupplier billingClientSupplier;
    private final PurchaseFetcher purchaseFetcher;
    private final SkuDetailsFetcher skuDetailsFetcher;
    private final SkuPurchaser skuPurchaser;
    private final BillingAvailabilityChecker billingAvailabilityChecker;

    public GoogleBillingr(InitializedBillingClientSupplier billingClientSupplier,
                          PurchaseFetcher purchaseFetcher,
                          SkuDetailsFetcher skuDetailsFetcher,
                          SkuPurchaser skuPurchaser,
                          BillingAvailabilityChecker billingAvailabilityChecker) {
        this.billingClientSupplier = billingClientSupplier;
        this.purchaseFetcher = purchaseFetcher;
        this.skuDetailsFetcher = skuDetailsFetcher;
        this.skuPurchaser = skuPurchaser;
        this.billingAvailabilityChecker = billingAvailabilityChecker;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
        purchaseFetcher.fetchInAppPurchases(listener);
    }

    @Override
    public void fetchInAppSkuDetails(List<String> skus, SkuDetailsFetcher.Listener listener) {
        skuDetailsFetcher.fetchInAppSkuDetails(skus, listener);
    }

    @Override
    public void addObserver(PurchaseListener observer) {
        skuPurchaser.addObserver(observer);
    }

    @Override
    public void removeObserver(PurchaseListener observer) {
        skuPurchaser.removeObserver(observer);
    }

    @Override
    public void forEachObserver(Consumer<PurchaseListener> observerConsumer) {
        skuPurchaser.forEachObserver(observerConsumer);
    }

    @Override
    public void removeObservers() {
        skuPurchaser.removeObservers();
    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, SkuPurchaser.Listener listener) {
        skuPurchaser.launchPurchaseFlow(sku, activity, listener);
    }

    @Override
    public void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest) {
        skuPurchaser.launchPurchaseFlow(purchaseSkuRequest);
    }

    @Override
    public void setPurchasesProcessor(PurchasesProcessor purchasesProcessor) {
        purchaseFetcher.setPurchasesProcessor(purchasesProcessor);
    }

    @Override
    public void checkBillingAvailability(Callback callback) {
        billingAvailabilityChecker.checkBillingAvailability(callback);
    }
}
