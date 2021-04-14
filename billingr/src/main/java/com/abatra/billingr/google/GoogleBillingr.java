package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingAvailabilityChecker;
import com.abatra.billingr.Billingr;
import com.abatra.billingr.DefaultPurchaseListener;
import com.abatra.billingr.purchase.AcknowledgePurchaseCallback;
import com.abatra.billingr.purchase.AcknowledgePurchasesCallback;
import com.abatra.billingr.purchase.ConsumePurchaseCallback;
import com.abatra.billingr.purchase.ConsumePurchasesCallback;
import com.abatra.billingr.purchase.DefaultAcknowledgePurchasesCallback;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.abatra.billingr.purchase.PurchaseFetcher;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.PurchaseSkuRequest;
import com.abatra.billingr.purchase.SkuPurchase;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.sku.SkuDetailsFetcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class GoogleBillingr implements Billingr {

    final PurchaseFetcher purchaseFetcher;
    final SkuDetailsFetcher skuDetailsFetcher;
    final SkuPurchaser skuPurchaser;
    final BillingAvailabilityChecker availabilityChecker;
    final PurchaseConsumer purchaseConsumer;
    final PurchaseAcknowledger purchaseAcknowledger;

    public GoogleBillingr(PurchaseFetcher purchaseFetcher,
                          SkuDetailsFetcher skuDetailsFetcher,
                          SkuPurchaser skuPurchaser,
                          BillingAvailabilityChecker availabilityChecker,
                          PurchaseConsumer purchaseConsumer,
                          PurchaseAcknowledger purchaseAcknowledger) {
        this.purchaseFetcher = purchaseFetcher;
        this.skuDetailsFetcher = skuDetailsFetcher;
        this.skuPurchaser = skuPurchaser;
        this.availabilityChecker = availabilityChecker;
        this.purchaseConsumer = purchaseConsumer;
        this.purchaseAcknowledger = purchaseAcknowledger;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        purchaseFetcher.observeLifecycle(lifecycleOwner);
        skuDetailsFetcher.observeLifecycle(lifecycleOwner);
        skuPurchaser.observeLifecycle(lifecycleOwner);
        availabilityChecker.observeLifecycle(lifecycleOwner);
        purchaseConsumer.observeLifecycle(lifecycleOwner);
        purchaseAcknowledger.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void setPurchaseListener(PurchaseListener purchaseListener) {
        skuPurchaser.setPurchaseListener(purchaseListener);
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
    public void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest) {
        skuPurchaser.launchPurchaseFlow(purchaseSkuRequest);
    }

    @Override
    public void checkBillingAvailability(BillingAvailabilityChecker.Callback callback) {
        availabilityChecker.checkBillingAvailability(callback);
    }

    @Override
    public void acknowledgePurchase(SkuPurchase skuPurchase, AcknowledgePurchaseCallback callback) {
        purchaseAcknowledger.acknowledgePurchase(skuPurchase, callback);
    }

    @Override
    public void consumePurchase(SkuPurchase skuPurchase, ConsumePurchaseCallback callback) {
        purchaseConsumer.consumePurchase(skuPurchase, callback);
    }

    @Override
    public void consumePurchases(List<SkuPurchase> skuPurchases, ConsumePurchasesCallback callback) {
        purchaseConsumer.consumePurchases(skuPurchases, callback);
    }

    @Override
    public void acknowledgePurchases(List<SkuPurchase> skuPurchases, AcknowledgePurchasesCallback callback) {
        purchaseAcknowledger.acknowledgePurchases(skuPurchases, callback);
    }

    @Override
    public void fetchInAppPurchasesAndAck() {
        purchaseFetcher.fetchInAppPurchases(new DefaultPurchaseListener() {

            @Override
            public void onPurchasesLoaded(List<SkuPurchase> skuPurchases) {
                acknowledgePurchases(filterPurchasesToAck(skuPurchases), DefaultAcknowledgePurchasesCallback.INSTANCE);
            }
        });
    }

    @NotNull
    private List<SkuPurchase> filterPurchasesToAck(List<SkuPurchase> skuPurchases) {
        return skuPurchases.stream()
                .filter(sp -> sp.isPurchased() && !sp.isAcknowledged())
                .collect(Collectors.toList());
    }
}
