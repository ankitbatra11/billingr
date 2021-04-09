package com.abatra.billingr.google;

import com.abatra.billingr.BillingUnavailableCallback;
import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingClient;

import org.mockito.Mockito;

import java.util.function.Consumer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class InitializedBillingClientSupplierMocker {

    public static final RuntimeException GET_ERROR = new RuntimeException("Simulated get failure");

    public static InitializedBillingClientSupplier mockUnavailable() {
        return mockBehavior(BillingUnavailableCallback::onBillingUnavailable);
    }

    private static InitializedBillingClientSupplier mockBehavior(Consumer<InitializedBillingClientSupplier.Listener> listenerConsumer) {

        InitializedBillingClientSupplier supplier = mock(InitializedBillingClientSupplier.class);

        doAnswer(invocation ->
        {
            InitializedBillingClientSupplier.Listener listener = invocation.getArgument(0);
            listenerConsumer.accept(listener);
            return null;

        }).when(supplier).getInitializedBillingClient(any());

        return supplier;
    }

    public static InitializedBillingClientSupplier mockInitialized(BillingClient billingClient) {
        return mockBehavior(listener -> listener.initialized(billingClient));
    }

    public static InitializedBillingClientSupplier mockInitializationFailure(BillingrException error) {
        return mockBehavior(listener -> listener.initializationFailed(error));
    }

    public static InitializedBillingClientSupplier mockGetClientFailure() {
        InitializedBillingClientSupplier supplier = mock(InitializedBillingClientSupplier.class);
        doThrow(GET_ERROR).when(supplier).getInitializedBillingClient(any());
        return supplier;
    }
}
