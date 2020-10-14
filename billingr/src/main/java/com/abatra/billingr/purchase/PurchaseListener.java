package com.abatra.billingr.purchase;

import com.abatra.billingr.exception.LoadingPurchasesFailedException;

import java.util.List;

public interface PurchaseListener {

    default void onPurchasesUpdated(List<Purchase> purchases) {
    }

    default void onLoadingPurchasesFailed(LoadingPurchasesFailedException e) {
    }
}
