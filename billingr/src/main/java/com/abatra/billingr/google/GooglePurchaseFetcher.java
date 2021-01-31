package com.abatra.billingr.google;

import com.abatra.billingr.PurchaseFetcher;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.util.BillingUtils;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class GooglePurchaseFetcher implements PurchaseFetcher {

    private final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseFetcher(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void fetchInAppPurchases(PurchaseListener listener) {
        fetchInAppPurchases(listener, BillingUtils::isPurchased);
    }

    private void fetchInAppPurchases(PurchaseListener listener, Predicate<Purchase> purchasePredicate) {
        billingClientSupplier.getInitializedBillingClient(billingClient -> {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            if (BillingUtils.isOk(purchasesResult.getBillingResult())) {
                List<SkuPurchase> skuPurchases = new ArrayList<>();
                if (purchasesResult.getPurchasesList() != null) {
                    for (Purchase purchase : purchasesResult.getPurchasesList()) {
                        if (purchasePredicate.apply(purchase)) {
                            skuPurchases.add(new GoogleSkuPurchase(purchase));
                        }
                    }
                }
                listener.updated(skuPurchases);
            }
        });
    }

    @Override
    public void fetchUnacknowledgedInAppPurchases(PurchaseListener listener) {
        fetchInAppPurchases(listener, purchase -> !Objects.requireNonNull(purchase).isAcknowledged());
    }
}
