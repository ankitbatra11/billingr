package com.abatra.billingr.google;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.util.BillingUtils;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class InitializedBillingClientSupplier implements Observable<PurchaseListener>, PurchasesUpdatedListener {

    private BillingClient billingClient;
    private final AtomicBoolean retriedInitializing = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final Observable<Listener> listeners = Observable.copyOnWriteArraySet();
    private final Observable<PurchaseListener> purchaseListeners = Observable.hashSet();

    private InitializedBillingClientSupplier() {
    }

    public static InitializedBillingClientSupplier newInstance(Context context) {

        InitializedBillingClientSupplier supplier = new InitializedBillingClientSupplier();
        supplier.billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(supplier)
                .build();

        return supplier;
    }

    @Override
    public void addObserver(PurchaseListener observer) {
        purchaseListeners.addObserver(observer);
    }

    @Override
    public void removeObserver(PurchaseListener observer) {
        purchaseListeners.addObserver(observer);
    }

    @Override
    public void forEachObserver(Consumer<PurchaseListener> observerConsumer) {
        purchaseListeners.forEachObserver(observerConsumer);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (BillingUtils.isOk(billingResult)) {
            List<SkuPurchase> skuPurchases = new ArrayList<>();
            if (list != null) {
                for (Purchase purchase : list) {
                    skuPurchases.add(new GoogleSkuPurchase(purchase));
                }
            }
            forEachObserver(purchaseListener -> purchaseListener.updated(skuPurchases));
        } else {
            Timber.w("Unexpected billingResult=%s for onPurchasesUpdated", billingResult);
        }
    }

    public void getInitializedBillingClient(Listener listener) {

        listeners.addObserver(listener);

        if (billingClient.isReady()) {
            listener.initialized(billingClient);
        } else {
            if (!connecting.get()) {
                startConnection();
            } else {
                Timber.d("Already connecting to google play!");
            }
        }
    }

    private void startConnection() {

        retriedInitializing.set(false);
        connecting.set(true);

        assert billingClient != null;
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                connecting.set(false);

                if (BillingUtils.isOk(billingResult)) {
                    listeners.forEachObserver(listener -> listener.initialized(billingClient));
                } else {
                    Timber.w("unexpected billingResult=%s from onBillingSetupFinished", BillingUtils.toString(billingResult));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

                connecting.set(false);

                if (!retriedInitializing.getAndSet(true)) {
                    startConnection();
                }
            }
        });
    }

    public interface Listener {
        void initialized(BillingClient billingClient);
    }
}
