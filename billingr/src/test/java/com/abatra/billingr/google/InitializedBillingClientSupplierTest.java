package com.abatra.billingr.google;

import android.os.Build;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class InitializedBillingClientSupplierTest {

    @Mock
    private BillingClientFactory mockedBillingClientFactory;

    @InjectMocks
    private InitializedBillingClientSupplier initializedBillingClientSupplier;

    @Mock
    private BillingClient mockedBillingClient;

    @Captor
    private ArgumentCaptor<BillingClientStateListener> clientStateListenerArgumentCaptor;

    @Mock
    private InitializedBillingClientSupplier.Listener mockedListener;

    @Mock
    private BillingResult mockedBillingResult;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockedBillingClientFactory.createPendingPurchasesEnabledBillingClient(any(PurchasesUpdatedListener.class)))
                .thenReturn(mockedBillingClient);
    }

    @Test
    public void testGetInitializedBillingClient_billingUnavailable() {

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);

        verify(mockedBillingClient, times(1)).startConnection(clientStateListenerArgumentCaptor.capture());

        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE);
        clientStateListenerArgumentCaptor.getValue().onBillingSetupFinished(mockedBillingResult);

        verify(mockedListener, times(1)).onBillingUnavailable();
        verifyNoMoreInteractions(mockedListener);
    }
}
