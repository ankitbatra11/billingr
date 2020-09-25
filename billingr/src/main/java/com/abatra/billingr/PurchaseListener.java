package com.abatra.billingr;

import java.util.List;

public interface PurchaseListener {
    void onPurchasesUpdated(List<Purchase> purchases);
}
