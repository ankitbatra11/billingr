package com.abatra.billingr.google;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abatra.android.wheelie.lifecycle.observer.ILifecycleObserver;
import com.abatra.android.wheelie.lifecycle.owner.ILifecycleOwner;
import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.PurchasesNotifier;
import com.abatra.billingr.purchase.SkuPurchase;
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
import java.util.function.Function;

import timber.log.Timber;

public class InitializedBillingClientSupplier implements ILifecycleObserver, PurchasesNotifier {

    final Function<PurchasesUpdatedListener, BillingClient> billingClientFactory;

    final AtomicBoolean retriedInitializing = new AtomicBoolean(false);
    final AtomicBoolean connecting = new AtomicBoolean(false);

    @Nullable
    private com.abatra.billingr.purchase.PurchaseListener purchaseListener;
    @Nullable
    private Listener listener;
    @Nullable
    BillingClient billingClient;

    public InitializedBillingClientSupplier(Function<PurchasesUpdatedListener, BillingClient> billingClientFactory) {
        this.billingClientFactory = billingClientFactory;
    }

    @Override
    public void observeLifecycle(ILifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public void setPurchaseListener(@Nullable com.abatra.billingr.purchase.PurchaseListener purchaseListener) {
        this.purchaseListener = purchaseListener;
    }

    public void getInitializedBillingClient(Listener listener) {
        this.listener = listener;
        if (!connecting.get()) {
            retriedInitializing.set(false);
            endConnectionBuildClientStartConnectionOrThrow();
        } else {
            Timber.d("Already connecting to google play!");
        }
    }

    private void endConnectionBuildClientStartConnectionOrThrow() {
        connecting.set(true);
        try {
            tryEndingConnectionBuildingClientStartingConnection();
        } catch (Throwable error) {
            connecting.set(false);
            initializationFailed(new GoogleBillingrException(error));
        }
    }

    private void tryEndingConnectionBuildingClientStartingConnection() {

        endConnection();

        PurchaseListener purchasesUpdatedListener = new PurchaseListener(new WeakReference<>(this));
        billingClient = billingClientFactory.apply(purchasesUpdatedListener);
        billingClient.startConnection(new ConnectionListener(new WeakReference<>(this)));
    }

    private void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }

    private void onBillingSetupFinished(@NotNull BillingResult billingResult) {

        connecting.set(false);

        if (GoogleBillingUtils.isOk(billingResult)) {
            getListener().ifPresent(l -> l.initialized(billingClient));
        } else if (GoogleBillingUtils.isUnavailable(billingResult)) {
            getListener().ifPresent(Listener::onBillingUnavailable);
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
            endConnectionBuildClientStartConnectionOrThrow();
        } else {
            initializationFailed("Connection retry exhausted!");
        }
    }

    private void initializationFailed(String message) {
        initializationFailed(new GoogleBillingrException(message));
    }

    private void initializationFailed(BillingrException billingrException) {
        getListener().ifPresent(l -> l.initializationFailed(billingrException));
    }

    private void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (GoogleBillingUtils.isOk(billingResult)) {
            if (list != null) {
                List<SkuPurchase> skuPurchases = new ArrayList<>();
                for (Purchase purchase : list) {
                    skuPurchases.add(new GoogleSkuPurchase(purchase));
                }
                if (skuPurchases.isEmpty()) {
                    Timber.i("purchase list is empty");
                } else {
                    Optional.ofNullable(purchaseListener).ifPresent(pl -> pl.onPurchasesLoaded(skuPurchases));
                }
            } else {
                Timber.w("billing result is OK purchase list is null");
            }
        } else {
            Timber.w("Unexpected billingResult=%s for onPurchasesUpdated", GoogleBillingUtils.toString(billingResult));
        }
    }

    @Override
    public void onDestroy() {
        listener = null;
        purchaseListener = null;
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

    public interface Listener extends BillingUnavailableCallback {

        void initialized(BillingClient billingClient);

        void initializationFailed(BillingrException billingrException);
    }
}
