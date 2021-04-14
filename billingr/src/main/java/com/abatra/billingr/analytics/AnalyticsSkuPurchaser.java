package com.abatra.billingr.analytics;

import androidx.annotation.Nullable;

import com.abatra.android.wheelie.chronicle.Chronicle;
import com.abatra.android.wheelie.chronicle.model.BeginCheckoutEventParams;
import com.abatra.android.wheelie.chronicle.model.Price;
import com.abatra.android.wheelie.chronicle.model.PurchasableItem;
import com.abatra.android.wheelie.chronicle.model.PurchaseEventParams;
import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.PurchaseSkuRequest;
import com.abatra.billingr.purchase.SkuPurchase;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.abatra.billingr.sku.Sku;

import java.util.List;
import java.util.Optional;

public class AnalyticsSkuPurchaser implements SkuPurchaser, PurchaseListener.NoOpPurchaseListener {

    public final SkuPurchaser delegate;
    @Nullable
    Sku checkedOutSku;

    public AnalyticsSkuPurchaser(SkuPurchaser delegate) {
        this.delegate = delegate;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        delegate.observeLifecycle(lifecycleOwner);
    }

    @Override
    public void setPurchaseListener(PurchaseListener purchaseListener) {
        delegate.setPurchaseListener(new PurchaseListener() {
            @Override
            public void onBillingUnavailable() {
                purchaseListener.onBillingUnavailable();
            }

            @Override
            public void onPurchasesLoaded(List<SkuPurchase> skuPurchases) {
                purchaseListener.onPurchasesLoaded(skuPurchases);
                Optional.ofNullable(checkedOutSku).ifPresent(sku -> logPurchaseEvent(sku, skuPurchases));
            }

            @Override
            public void onPurchasesLoadFailed(BillingrException error) {
                purchaseListener.onPurchasesLoadFailed(error);
            }
        });
    }

    private void logPurchaseEvent(Sku sku, List<SkuPurchase> skuPurchases) {
        skuPurchases.stream()
                .filter(SkuPurchase::isPurchased)
                .filter(skuPurchase -> skuPurchase.getSku().equalsIgnoreCase(sku.getId()))
                .findFirst()
                .ifPresent(skuPurchase -> Chronicle.recordPurchaseEvent(createPurchaseEventParams(sku, skuPurchase)));
    }

    private PurchaseEventParams createPurchaseEventParams(Sku sku, SkuPurchase skuPurchase) {
        return new PurchaseEventParams()
                .setAffiliation(sku.getAffiliation())
                .setPrice(createPrice(sku))
                .setTransactionId(skuPurchase.getPurchaseToken())
                .addPurchasedItem(createPurchasableItem(sku));
    }

    private Price createPrice(Sku sku) {
        return new Price(sku.getPriceAmount(), sku.getCurrency());
    }

    private PurchasableItem createPurchasableItem(Sku sku) {
        return new PurchasableItem()
                .setId(sku.getId())
                .setName(sku.getTitle())
                .setCategory(sku.getType().asPurchasableItemCategory())
                .setPrice(sku.getPriceAmount())
                .setQuantity(1);
    }

    private void logBeginCheckoutEvent(Sku checkedOutSku) {
        Chronicle.recordBeginCheckoutEvent(createBeginCheckoutEventParams(checkedOutSku));
    }

    private BeginCheckoutEventParams createBeginCheckoutEventParams(Sku checkedOutSku) {
        return new BeginCheckoutEventParams()
                .setPrice(createPrice(checkedOutSku))
                .addItem(createPurchasableItem(checkedOutSku));
    }

    @Override
    public void launchPurchaseFlow(PurchaseSkuRequest purchaseSkuRequest) {
        Optional<Listener> listener = purchaseSkuRequest.getListener();
        delegate.launchPurchaseFlow(new PurchaseSkuRequest(purchaseSkuRequest).setListener(new Listener() {

            @Override
            public void onBillingUnavailable() {
                listener.ifPresent(BillingUnavailableCallback::onBillingUnavailable);
            }

            @Override
            public void onPurchaseFlowLaunchedSuccessfully() {
                checkedOutSku = purchaseSkuRequest.getSku();
                logBeginCheckoutEvent(checkedOutSku);
                listener.ifPresent(Listener::onPurchaseFlowLaunchedSuccessfully);
            }

            @Override
            public void onPurchaseFlowLaunchFailed(BillingrException billingrException) {
                listener.ifPresent(l -> l.onPurchaseFlowLaunchFailed(billingrException));
            }
        }));
    }
}
