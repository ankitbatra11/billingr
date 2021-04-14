package com.abatra.billingr.google;

import androidx.annotation.NonNull;

import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.Purchase;

import static com.abatra.billingr.google.GoogleBillingUtils.*;

public class GoogleSkuPurchase implements SkuPurchase {

    private final Purchase purchase;

    public GoogleSkuPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    @Override
    public String getSku() {
        return purchase.getSku();
    }

    @Override
    public String getPurchaseToken() {
        return purchase.getPurchaseToken();
    }

    @Override
    public boolean isAcknowledgedPurchased() {
        return purchase.isAcknowledged() && isPurchased(purchase);
    }

    public Purchase getPurchase() {
        return purchase;
    }

    @NonNull
    @Override
    public String toString() {
        return "GoogleSkuPurchase{" +
                "purchase=" + purchase +
                '}';
    }
}
