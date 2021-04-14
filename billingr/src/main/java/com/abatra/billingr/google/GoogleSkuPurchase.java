package com.abatra.billingr.google;

import androidx.annotation.NonNull;

import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.Purchase;

import static com.abatra.billingr.google.GoogleBillingUtils.*;

public class GoogleSkuPurchase implements SkuPurchase {

    final Purchase purchase;

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
    public boolean isPurchased() {
        return GoogleBillingUtils.isPurchased(purchase);
    }

    @Override
    public boolean isAcknowledged() {
        return purchase.isAcknowledged();
    }

    @NonNull
    @Override
    public String toString() {
        return "GoogleSkuPurchase{" +
                "purchase=" + purchase +
                '}';
    }
}
