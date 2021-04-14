package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingAvailabilityChecker;
import com.abatra.billingr.BillingrException;
import com.android.billingclient.api.BillingClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.abatra.billingr.google.GoogleBillingrException.*;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitializationFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitialized;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockUnavailable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GoogleBillingAvailabilityCheckerTest {

    @Mock
    private BillingClient mockedBillingClient;

    private GoogleBillingAvailabilityChecker availabilityChecker;

    @Mock
    private BillingAvailabilityChecker.Callback mockedCallback;

    @Mock
    private ILifecycleOwner mockedLifecycleOwner;

    @Test
    public void test_unavailable() {

        availabilityChecker = new GoogleBillingAvailabilityChecker(mockUnavailable());

        availabilityChecker.checkBillingAvailability(mockedCallback);

        verify(mockedCallback, times(1)).onBillingUnavailable();
    }

    @Test
    public void test_error() {
        BillingrException error = unavailable();
        availabilityChecker = new GoogleBillingAvailabilityChecker(mockInitializationFailure(error));

        availabilityChecker.checkBillingAvailability(mockedCallback);

        verify(mockedCallback, times(1)).onBillingAvailabilityCheckFailed(unavailable());
    }

    @Test
    public void test_available() {

        availabilityChecker = new GoogleBillingAvailabilityChecker(mockInitialized(mockedBillingClient));

        availabilityChecker.checkBillingAvailability(mockedCallback);

        verify(mockedCallback, times(1)).onBillingAvailable(mockedBillingClient);
    }

    @Test
    public void test_observeLifecycle() {

        InitializedBillingClientSupplier supplier = mockUnavailable();
        availabilityChecker = new GoogleBillingAvailabilityChecker(supplier);

        availabilityChecker.observeLifecycle(mockedLifecycleOwner);

        verify(supplier, times(1)).observeLifecycle(mockedLifecycleOwner);
    }
}
