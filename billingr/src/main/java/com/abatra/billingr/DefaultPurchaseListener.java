package com.abatra.billingr;

import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.SkuPurchase;

import java.util.List;

public interface DefaultPurchaseListener extends PurchaseListener {

    @Override
    default void onBillingUnavailable() {

    }

    @Override
    default void onPurchasesLoaded(List<SkuPurchase> skuPurchases) {

    }

    @Override
    default void onPurchasesLoadFailed(BillingrException error) {

    }
}
