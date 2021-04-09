package com.abatra.billingr.google;

import android.os.Build;

import com.abatra.billingr.purchase.PurchaseListener;

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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class GooglePurchaseFetcherTest {

    @Mock
    private InitializedBillingClientSupplier mockedInitializedBillingClientSupplier;

    @InjectMocks
    private GooglePurchaseFetcher googlePurchaseFetcher;

    @Mock
    private PurchaseListener mockedPurchaseListener;

    @Captor
    private ArgumentCaptor<InitializedBillingClientSupplier.Listener> listenerArgumentCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFetchInAppPurchases_billingUnavailable() {

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedInitializedBillingClientSupplier, times(1)).getInitializedBillingClient(listenerArgumentCaptor.capture());

        listenerArgumentCaptor.getValue().onBillingUnavailable();

        verify(mockedPurchaseListener, times(1)).onBillingUnavailable();
        verifyNoMoreInteractions(mockedPurchaseListener);
    }
}
