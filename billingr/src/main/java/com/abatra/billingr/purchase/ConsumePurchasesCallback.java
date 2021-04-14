package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingrException;

public interface ConsumePurchasesCallback extends ConsumePurchaseCallback {
    void onPurchasesConsumptionProcessFailure(BillingrException billingrException);
}
