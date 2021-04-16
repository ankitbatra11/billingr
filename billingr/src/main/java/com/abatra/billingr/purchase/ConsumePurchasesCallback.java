package com.abatra.billingr.purchase;

import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;

public interface ConsumePurchasesCallback extends BillingUnavailableCallback {

    void onPurchasesConsumptionFailure(BillingrException billingrException);

    void onPurchasesConsumptionResultUpdated(PurchasesConsumptionResult purchasesConsumptionResult);
}
