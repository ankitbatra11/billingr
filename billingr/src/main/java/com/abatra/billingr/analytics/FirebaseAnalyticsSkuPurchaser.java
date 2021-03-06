package com.abatra.billingr.analytics;

import com.abatra.android.wheelie.chronicle.BeginCheckoutEventParams;
import com.abatra.android.wheelie.chronicle.PurchaseEventParams;
import com.abatra.android.wheelie.chronicle.firebase.FirebaseBeginCheckoutEventParams;
import com.abatra.android.wheelie.chronicle.firebase.FirebasePrice;
import com.abatra.android.wheelie.chronicle.firebase.FirebasePurchasableItem;
import com.abatra.android.wheelie.chronicle.firebase.FirebasePurchaseEventParams;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.SkuPurchaser;

public class FirebaseAnalyticsSkuPurchaser extends AnalyticsSkuPurchaser {

    public FirebaseAnalyticsSkuPurchaser(SkuPurchaser delegate) {
        super(delegate);
    }

    @Override
    protected PurchaseEventParams createPurchaseEventParams(Sku sku, SkuPurchase skuPurchase) {
        return new FirebasePurchaseEventParams()
                .setAffiliation(sku.getAffiliation())
                .setFirebasePrice(createPrice(sku))
                .setTransactionId(skuPurchase.getPurchaseToken())
                .addPurchasedItem(createPurchasableItem(sku));
    }

    @Override
    protected BeginCheckoutEventParams createBeginCheckoutEventParams(Sku checkedOutSku) {
        return new FirebaseBeginCheckoutEventParams()
                .setFirebasePrice(createPrice(checkedOutSku))
                .addCheckedOutItem(createPurchasableItem(checkedOutSku));
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
}
