package com.abatra.billingr;

public interface BillingUseCase {

    void loadBilling(LoadBillingRequest loadBillingRequest);

    void querySkus(QuerySkuRequest querySkuRequest);

    void queryPurchases(QueryPurchasesRequest queryPurchasesRequest);

    void destroy();
}
