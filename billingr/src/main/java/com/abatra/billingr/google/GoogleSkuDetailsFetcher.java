package com.abatra.billingr.google;

import com.abatra.billingr.SkuDetailsFetcher;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuType;
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
    public void fetchInAppSkuDetails(List<String> skus, Listener listener) {

        billingClientSupplier.getInitializedBillingClient(new InitializedBillingClientSupplier.Listener() {
            @Override
            public void initialized(BillingClient billingClient) {

                SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                        .setType(BillingClient.SkuType.INAPP)
                        .setSkusList(skus)
                        .build();

                billingClient.querySkuDetailsAsync(skuDetailsParams, (billingResult, list) -> {

                    if (GoogleBillingUtils.isOk(billingResult)) {
                        List<Sku> loadedSkus = new ArrayList<>();
                        if (list != null) {
                            for (SkuDetails skuDetails : list) {
                                loadedSkus.add(new GoogleSku(SkuType.IN_APP_PRODUCT, skuDetails));
                            }
                        }
                        listener.skusLoaded(loadedSkus);
                    } else {

                        Timber.w("unexpected billing result=%s from querySkuDetailsAsync for in app sku type",
                                GoogleBillingUtils.toString(billingResult));

                        listener.loadingSkuDetailsFailed(GoogleBillingrException.from(billingResult));
                    }
                });
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                listener.loadingSkuDetailsFailed(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                listener.onBillingUnavailable();
            }
        });

    }
}
