package com.abatra.billingr;

import com.abatra.billingr.purchase.PurchaseListener;

public interface PurchasesNotifier {
    void setPurchaseListener(PurchaseListener purchaseListener);
}
