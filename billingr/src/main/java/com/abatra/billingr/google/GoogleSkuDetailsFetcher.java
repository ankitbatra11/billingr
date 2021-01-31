package com.abatra.billingr.google;

import com.abatra.billingr.SkuDetailsFetcher;
import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuType;
import com.abatra.billingr.util.BillingUtils;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GoogleSkuDetailsFetcher implements SkuDetailsFetcher {

    private final InitializedBillingClientSupplier billingClientSupplier;

    public GoogleSkuDetailsFetcher(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void fetchInAppSkuDetails(Listener listener) {

        billingClientSupplier.getInitializedBillingClient(billingClient -> {

            SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(skuDetailsParams, (billingResult, list) -> {
                if (BillingUtils.isOk(billingResult)) {
                    List<Sku> skus = new ArrayList<>();
                    if (list != null) {
                        for (SkuDetails skuDetails : list) {
                            skus.add(new GoogleSku(SkuType.IN_APP_PRODUCT, skuDetails));
                        }
                    }
                    listener.loaded(skus);
                } else {
                    Timber.w("unexpected billing result=%s from querySkuDetailsAsync for in app sku type",
                            BillingUtils.toString(billingResult));
                }
            });
        });

    }
}
