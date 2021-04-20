package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.owner.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.ConsumePurchaseCallback;
import com.abatra.billingr.purchase.ConsumePurchasesCallback;
import com.abatra.billingr.purchase.DefaultConsumePurchaseCallback;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.abatra.billingr.purchase.PurchasesConsumptionResult;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ConsumeParams;

import java.util.List;
import java.util.function.Consumer;

import timber.log.Timber;

import static com.abatra.billingr.google.GoogleBillingUtils.isOk;
import static com.abatra.billingr.google.GoogleBillingUtils.reportErrorAndGet;

public class GooglePurchaseConsumer implements PurchaseConsumer {

    private final InitializedBillingClientSupplier billingClientSupplier;

    public GooglePurchaseConsumer(InitializedBillingClientSupplier billingClientSupplier) {
        this.billingClientSupplier = billingClientSupplier;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        billingClientSupplier.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void consumePurchase(SkuPurchase skuPurchase, ConsumePurchaseCallback callback) {
        getBillingClient(billingrException -> callback.onPurchaseConsumptionFailed(skuPurchase, billingrException), new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {
                GooglePurchaseConsumer.this.consumePurchase(billingClient, skuPurchase, callback);
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onPurchaseConsumptionFailed(skuPurchase, billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                callback.onBillingUnavailable();
            }
        });
    }

    private void consumePurchase(BillingClient billingClient, SkuPurchase skuPurchase, ConsumePurchaseCallback callback) {
        try {
            tryConsumingPurchase(skuPurchase, callback, billingClient);
        } catch (Throwable error) {
            Timber.e(error);
            callback.onPurchaseConsumptionFailed(skuPurchase, new GoogleBillingrException(error));
        }
    }

    private void getBillingClient(Consumer<BillingrException> errorConsumer,
                                  InitializedBillingClientSupplier.Listener listener) {
        try {
            billingClientSupplier.getInitializedBillingClient(listener);
        } catch (Throwable error) {
            Timber.e(error);
            errorConsumer.accept(new GoogleBillingrException(error));
        }
    }

    private void tryConsumingPurchase(SkuPurchase skuPurchase,
                                      ConsumePurchaseCallback callback,
                                      BillingClient billingClient) {

        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(skuPurchase.getPurchaseToken())
                .build();

        billingClient.consumeAsync(consumeParams, (billingResult, s) -> {
            if (isOk(billingResult)) {
                Timber.i("purchase=%s has been consumed!", skuPurchase);
                callback.onPurchaseConsumed(skuPurchase);
            } else {
                callback.onPurchaseConsumptionFailed(skuPurchase, reportErrorAndGet(billingResult, "Consuming purchase=%s failed!", skuPurchase));
            }
        });
    }

    @Override
    public void consumePurchases(List<SkuPurchase> skuPurchases, ConsumePurchasesCallback callback) {
        getBillingClient(callback::onPurchasesConsumptionFailure, new InitializedBillingClientSupplier.Listener() {

            @Override
            public void initialized(BillingClient billingClient) {
                consumePurchases(skuPurchases, callback, billingClient);
            }

            @Override
            public void initializationFailed(BillingrException billingrException) {
                callback.onPurchasesConsumptionFailure(billingrException);
            }

            @Override
            public void onBillingUnavailable() {
                callback.onBillingUnavailable();
            }
        });
    }

    private void consumePurchases(List<SkuPurchase> skuPurchases,
                                  ConsumePurchasesCallback callback,
                                  BillingClient billingClient) {

        PurchasesConsumptionResult result = new PurchasesConsumptionResult(skuPurchases);
        callback.onPurchasesConsumptionResultUpdated(result);

        for (SkuPurchase skuPurchase : skuPurchases) {
            consumePurchase(billingClient, skuPurchase, new DefaultConsumePurchaseCallback() {

                @Override
                public void onPurchaseConsumed(SkuPurchase skuPurchase) {
                    result.onPurchaseConsumed(skuPurchase);
                    callback.onPurchasesConsumptionResultUpdated(result);
                }

                @Override
                public void onPurchaseConsumptionFailed(SkuPurchase skuPurchase, BillingrException billingrException) {
                    result.onPurchaseConsumptionFailed(skuPurchase, billingrException);
                    callback.onPurchasesConsumptionResultUpdated(result);
                }

            });
        }
    }
}
