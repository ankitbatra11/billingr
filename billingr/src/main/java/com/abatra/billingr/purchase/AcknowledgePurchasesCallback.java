package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

public interface AcknowledgePurchasesCallback extends AcknowledgePurchaseCallback {
    void onPurchaseAcknowledgeProcessFailure(BillingrException error);
}
