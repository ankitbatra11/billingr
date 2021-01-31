package com.abatra.billingr;

public interface PurchaseAcknowledger {
    /**
     * @param listener Callback when a purchase is acknowledged.
     */
    void acknowledgeInAppPurchases(Listener listener);

    interface Listener {
        /**
         * Called when a purchase for an SKU is acknowledged.
         *
         * @param sku of which purchase is acknowledged.
         */
        void purchaseAcknowledged(String sku);
    }
}
