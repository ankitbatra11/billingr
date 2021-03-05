package com.abatra.billingr.google;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.android.wheelie.java8.Consumer;
import com.abatra.android.wheelie.lifecycle.LifecycleObserverObservable;
import com.abatra.android.wheelie.pattern.Observable;
import com.abatra.billingr.BillingAvailabilityCallback;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.PurchaseListener;
import com.abatra.billingr.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class InitializedBillingClientSupplier implements LifecycleObserverObservable<PurchaseListener> {

    private final BillingClientFactory billingClientFactory;

    private final AtomicBoolean retriedInitializing = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    @Nullable
    private Listener listener;
    private final Observable<com.abatra.billingr.PurchaseListener> purchaseListeners = Observable.hashSet();

    @Nullable
    private BillingClient billingClient;

    public InitializedBillingClientSupplier(BillingClientFactory billingClientFactory) {
        this.billingClientFactory = billingClientFactory;
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
        listener = null;
        purchaseListeners.removeObservers();
    }

    public void getInitializedBillingClient(Listener listener) {
        this.listener = listener;
        if (!connecting.get()) {
            retriedInitializing.set(false);
            endConnectionBuildClientStartConnection();
        } else {
            Timber.d("Already connecting to google play!");
        }
    }

    private void endConnectionBuildClientStartConnection() {

        endConnection();

        PurchaseListener purchasesUpdatedListener = new PurchaseListener(new WeakReference<>(this));
        billingClient = billingClientFactory.createPendingPurchasesEnabledBillingClient(purchasesUpdatedListener);

        startConnection();
    }

    private void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }

    private void startConnection() {

        connecting.set(true);

        assert billingClient != null;
        billingClient.startConnection(new ConnectionListener(new WeakReference<>(this)));
    }

    private void onBillingSetupFinished(@NotNull BillingResult billingResult) {

        connecting.set(false);

        if (GoogleBillingUtils.isOk(billingResult)) {
            getListener().ifPresent(l -> l.initialized(billingClient));
        } else if (GoogleBillingUtils.isUnavailable(billingResult)) {
            getListener().ifPresent(BillingAvailabilityCallback::onBillingUnavailable);
        } else {
            Timber.w("unexpected billingResult=%s from onBillingSetupFinished",
                    GoogleBillingUtils.toString(billingResult));

            initializationFailed(GoogleBillingUtils.toString(billingResult));
        }
    }

    private Optional<Listener> getListener() {
        return Optional.ofNullable(listener);
    }

    private void onBillingServiceDisconnected() {

        connecting.set(false);

        if (!retriedInitializing.getAndSet(true)) {
            endConnectionBuildClientStartConnection();
        } else {
            initializationFailed("Connection retry exhausted!");
        }
    }

    private void initializationFailed(String message) {
        getListener().ifPresent(l -> {
            GoogleBillingrException billingrException = new GoogleBillingrException(message);
            l.initializationFailed(billingrException);
        });
    }

    private void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
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
            getInitializedBillingClientSupplier().ifPresent(s -> s.onBillingSetupFinished(billingResult));
        }

        private Optional<InitializedBillingClientSupplier> getInitializedBillingClientSupplier() {
            return Optional.ofNullable(reference.get());
        }

        @Override
        public void onBillingServiceDisconnected() {
            getInitializedBillingClientSupplier().ifPresent(InitializedBillingClientSupplier::onBillingServiceDisconnected);
        }
    }

    private static class PurchaseListener implements PurchasesUpdatedListener {

        private final WeakReference<InitializedBillingClientSupplier> reference;

        private PurchaseListener(WeakReference<InitializedBillingClientSupplier> reference) {
            this.reference = reference;
        }

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            getInitializedBillingClientSupplier().ifPresent(s -> s.onPurchasesUpdated(billingResult, list));
        }

        private Optional<InitializedBillingClientSupplier> getInitializedBillingClientSupplier() {
            return Optional.ofNullable(reference.get());
        }
    }

    public interface Listener extends BillingAvailabilityCallback {

        default void initialized(BillingClient billingClient) {
        }

        default void initializationFailed(BillingrException billingrException) {
        }
    }
}
