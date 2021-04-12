package com.abatra.billingr.google;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingAvailabilityChecker;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.PurchaseSkuRequest;
import com.abatra.billingr.purchase.SkuPurchase;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.sku.SkuDetailsFetcher;

import java.util.List;

public class GoogleBillingr implements Billingr {

    final InitializedBillingClientSupplier billingClientSupplier;
    final PurchaseFetcher purchaseFetcher;
    final SkuDetailsFetcher skuDetailsFetcher;
    final SkuPurchaser skuPurchaser;
    final BillingAvailabilityChecker availabilityChecker;
    final PurchaseConsumer purchaseConsumer;
    final PurchaseAcknowledger purchaseAcknowledger;

    public GoogleBillingr(InitializedBillingClientSupplier billingClientSupplier,
                          PurchaseFetcher purchaseFetcher,
                          SkuDetailsFetcher skuDetailsFetcher,
                          SkuPurchaser skuPurchaser,
                          BillingAvailabilityChecker availabilityChecker,
                          PurchaseConsumer purchaseConsumer,
                          PurchaseAcknowledger purchaseAcknowledger) {
        this.billingClientSupplier = billingClientSupplier;
        this.purchaseFetcher = purchaseFetcher;
        this.skuDetailsFetcher = skuDetailsFetcher;
        this.skuPurchaser = skuPurchaser;
        this.availabilityChecker = availabilityChecker;
        this.purchaseConsumer = purchaseConsumer;
        this.purchaseAcknowledger = purchaseAcknowledger;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
        purchaseFetcher.observeLifecycle(lifecycleOwner);
        skuDetailsFetcher.observeLifecycle(lifecycleOwner);
        skuPurchaser.observeLifecycle(lifecycleOwner);
        availabilityChecker.observeLifecycle(lifecycleOwner);
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
    public void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest) {
        skuPurchaser.launchPurchaseFlow(purchaseSkuRequest);
    }

    @Override
    public void checkBillingAvailability(BillingAvailabilityChecker.Callback callback) {
        availabilityChecker.checkBillingAvailability(callback);
    }

    @Override
    public void acknowledgePurchase(SkuPurchase skuPurchase, PurchaseAcknowledger.Callback callback) {
        purchaseAcknowledger.acknowledgePurchase(skuPurchase, callback);
    }

    @Override
    public void consumePurchase(SkuPurchase skuPurchase, PurchaseConsumer.Callback callback) {
        purchaseConsumer.consumePurchase(skuPurchase, callback);
    }
}
