package com.abatra.billingr;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.abatra.android.wheelie.chronicle.Chronicle;
import com.abatra.android.wheelie.chronicle.firebase.FirebaseBeginCheckoutEventParams;
import com.abatra.android.wheelie.chronicle.firebase.FirebasePrice;
import com.abatra.android.wheelie.chronicle.firebase.FirebasePurchasableItem;
import com.abatra.android.wheelie.chronicle.firebase.FirebasePurchaseEventParams;

import java.util.List;
import java.util.Optional;

public class AnalyticsSkuPurchaser implements SkuPurchaser, PurchaseListener {

    private final SkuPurchaser delegate;
    @Nullable
    private Sku checkedOutSku;

    public AnalyticsSkuPurchaser(SkuPurchaser delegate) {
        this.delegate = delegate;
        this.delegate.addObserver(this);
    }

    @Override
    public void updated(List<SkuPurchase> skuPurchases) {
        Optional.ofNullable(checkedOutSku).ifPresent(sku -> logPurchaseEvent(sku, skuPurchases));
    }

    private void logPurchaseEvent(Sku sku, List<SkuPurchase> skuPurchases) {
        skuPurchases.stream()
                .filter(skuPurchase -> skuPurchase.getSku().equalsIgnoreCase(sku.getId()))
                .findFirst()
                .ifPresent(skuPurchase -> {
                    Chronicle.recordPurchaseEvent(new FirebasePurchaseEventParams()
                            .setAffiliation(sku.getAffiliation())
                            .setFirebasePrice(createPrice(sku))
                            .setTransactionId(skuPurchase.getPurchaseToken())
                            .addPurchasedItem(createPurchasableItem(sku))
                    );
                });
    }

    private FirebasePrice createPrice(Sku sku) {
        return new FirebasePrice(sku.getPriceAmount(), sku.getCurrency());
    }

    private FirebasePurchasableItem createPurchasableItem(Sku sku) {
        return new FirebasePurchasableItem()
                .setId(sku.getId())
                .setName(sku.getTitle())
                .setCategory(sku.getType().asPurchasableItemCategory())
                .setPrice(sku.getPriceAmount())
                .setQuantity(1);
    }

    @Override
    public void addObserver(PurchaseListener observer) {
        delegate.addObserver(observer);
    }

    @Override
    public void removeObserver(PurchaseListener observer) {
        delegate.removeObserver(observer);
    }

    @Override
    public void launchPurchaseFlow(Sku sku, Activity activity, Listener listener) {
        delegate.launchPurchaseFlow(sku, activity, new Listener() {

            @Override
            public void onBillingUnavailable() {
                listener.onBillingUnavailable();
            }

            @Override
            public void purchaseFlowLaunchedSuccessfully() {
                checkedOutSku = sku;
                logBeginCheckoutEvent(checkedOutSku);
                listener.purchaseFlowLaunchedSuccessfully();
            }

            @Override
            public void purchaseFlowLaunchFailed(BillingrException billingrException) {
                listener.purchaseFlowLaunchFailed(billingrException);
            }
        });
    }

    private void logBeginCheckoutEvent(Sku checkedOutSku) {
        Chronicle.recordBeginCheckoutEvent(new FirebaseBeginCheckoutEventParams()
                .setFirebasePrice(createPrice(checkedOutSku))
                .addCheckedOutItem(createPurchasableItem(checkedOutSku)));
    }
}
