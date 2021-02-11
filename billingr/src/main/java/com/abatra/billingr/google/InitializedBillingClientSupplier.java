package com.abatra.billingr.google;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.ILifecycleObserver;
import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.lang.ref.WeakReference;
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
    private final Observable<com.abatra.billingr.PurchaseListener> purchaseListeners = Observable.hashSet();

    @Nullable
    private BillingClient billingClient;

    public InitializedBillingClientSupplier(Context context) {
        this.context = context;
    }

    @Override
    public void addObserver(com.abatra.billingr.PurchaseListener observer) {
        purchaseListeners.addObserver(observer);
    }

    @Override
    public void removeObserver(com.abatra.billingr.PurchaseListener observer) {
        purchaseListeners.addObserver(observer);
    }

    @Override
    public void forEachObserver(Consumer<com.abatra.billingr.PurchaseListener> observerConsumer) {
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
                    reconnect();
                } else {
                    Timber.d("Already connecting to google play!");
                }
            }
        }
    }

    private void reconnect() {
        try {
            tryReconnecting();
        } catch (Throwable error) {
            GoogleBillingrException billingrException = new GoogleBillingrException(error);
            listeners.forEachObserver(type -> type.initializationFailed(billingrException));
        }
    }

    private void tryReconnecting() {
        endConnection();
        buildClientAndStartConnection();
    }

    private void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }

    private void buildClientAndStartConnection() {

        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(new PurchaseListener(new WeakReference<>(this)))
                .build();

        startConnection();
    }

    private void startConnection() {

        retriedInitializing.set(false);
        connecting.set(true);

        assert billingClient != null;
        billingClient.startConnection(new ConnectionListener(new WeakReference<>(this)));
    }

    private void onBillingSetupFinished(BillingResult billingResult) {

        connecting.set(false);

        if (GoogleBillingUtils.isOk(billingResult)) {
            listeners.forEachObserver(listener -> listener.initialized(billingClient));
        } else {
            Timber.w("unexpected billingResult=%s from onBillingSetupFinished",
                    GoogleBillingUtils.toString(billingResult));

            initializationFailed(GoogleBillingUtils.toString(billingResult));
        }
    }

    private void onBillingServiceDisconnected() {

        connecting.set(false);

        if (!retriedInitializing.getAndSet(true)) {
            startConnection();
        } else {
            initializationFailed("Connection retry exhausted!");
        }
    }

    private void initializationFailed(String message) {
        listeners.forEachObserver(listener -> listener.initializationFailed(new GoogleBillingrException(message)));
    }

    @Override
    public void onDestroy() {
        removeObservers();
        endConnection();
    }

    private static class ConnectionListener implements BillingClientStateListener {

        private final WeakReference<InitializedBillingClientSupplier> reference;

        private ConnectionListener(WeakReference<InitializedBillingClientSupplier> reference) {
            this.reference = reference;
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            if (reference.get() != null) {
                reference.get().onBillingSetupFinished(billingResult);
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            if (reference.get() != null) {
                reference.get().onBillingServiceDisconnected();
            }
        }
    }

    private static class PurchaseListener implements PurchasesUpdatedListener {

        private final WeakReference<InitializedBillingClientSupplier> reference;

        private PurchaseListener(WeakReference<InitializedBillingClientSupplier> reference) {
            this.reference = reference;
        }

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            if (reference.get() != null) {
                reference.get().onPurchasesUpdated(billingResult, list);
            }
        }
    }

    public interface Listener {

        default void initialized(BillingClient billingClient) {
        }

        default void initializationFailed(BillingrException billingrException) {
        }
    }
}
