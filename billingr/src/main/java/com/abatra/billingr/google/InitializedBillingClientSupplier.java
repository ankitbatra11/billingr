package com.abatra.billingr.google;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class InitializedBillingClientSupplier implements Observable<PurchaseListener>, PurchasesUpdatedListener,
        ILifecycleObserver {

    private final Context context;
    private final AtomicBoolean retriedInitializing = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final Observable<Listener> listeners = Observable.copyOnWriteArraySet();
    private final Observable<PurchaseListener> purchaseListeners = Observable.hashSet();

    @Nullable
    private BillingClient billingClient;

    public InitializedBillingClientSupplier(Context context) {
        this.context = context;
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
    public void removeObservers() {
        listeners.removeObservers();
        purchaseListeners.removeObservers();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (GoogleBillingUtils.isOk(billingResult)) {
            List<SkuPurchase> skuPurchases = new ArrayList<>();
            if (list != null) {
                for (Purchase purchase : list) {
                    skuPurchases.add(new GoogleSkuPurchase(purchase));
                }
            }
            forEachObserver(purchaseListener -> purchaseListener.updated(skuPurchases));
        } else {
            Timber.w("Unexpected billingResult=%s for onPurchasesUpdated", GoogleBillingUtils.toString(billingResult));
        }
    }

    public void getInitializedBillingClient(Listener listener) {

        listeners.addObserver(listener);

        if (billingClient == null) {
            buildClientAndStartConnection();
        } else {
            if (billingClient.isReady()) {
                listener.initialized(billingClient);
            } else {
                if (!connecting.get()) {
                    buildClientAndStartConnection();
                } else {
                    Timber.d("Already connecting to google play!");
                }
            }
        }
    }

    private void buildClientAndStartConnection() {
        try {
            tryBuildingClientAndStartConnection();
        } catch (Throwable error) {
            GoogleBillingrException billingrException = new GoogleBillingrException(error);
            listeners.forEachObserver(listener -> listener.initializationFailed(billingrException));
        }
    }

    private void tryBuildingClientAndStartConnection() {

        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        startConnection();
    }

    private void startConnection() {

        assert billingClient != null;

        retriedInitializing.set(false);
        connecting.set(true);

        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                connecting.set(false);

                if (GoogleBillingUtils.isOk(billingResult)) {
                    listeners.forEachObserver(listener -> listener.initialized(billingClient));
                } else {
                    Timber.w("unexpected billingResult=%s from onBillingSetupFinished",
                            GoogleBillingUtils.toString(billingResult));

                    initializationFailed(GoogleBillingUtils.toString(billingResult));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

                connecting.set(false);

                if (!retriedInitializing.getAndSet(true)) {
                    startConnection();
                } else {
                    initializationFailed("Connection retry exhausted!");
                }
            }
        });
    }

    private void initializationFailed(String message) {
        listeners.forEachObserver(listener -> listener.initializationFailed(new GoogleBillingrException(message)));
    }

    @Override
    public void onDestroy() {

        removeObservers();

        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }

    public interface Listener {

        default void initialized(BillingClient billingClient) {
        }

        default void initializationFailed(BillingrException billingrException) {
        }
    }
}
