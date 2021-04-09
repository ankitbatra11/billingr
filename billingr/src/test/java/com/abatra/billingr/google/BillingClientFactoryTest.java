package com.abatra.billingr.google;

import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BillingClientFactoryTest {

    @Mock
    private Context mockedContext;

    @InjectMocks
    private BillingClientFactory billingClientFactory;

    @Mock
    private BillingClient.Builder mockedBuilder;

    @Mock
    private PurchasesUpdatedListener mockedPurchasesUpdatedListener;

    @Mock
    private BillingClient mockedBillingClient;

    @Test
    public void test_createPendingPurchasesEnabledBillingClient() {
        try (MockedStatic<BillingClient> billingClientMockedStatic = mockStatic(BillingClient.class)) {

            billingClientMockedStatic.when(() -> BillingClient.newBuilder(mockedContext)).thenReturn(mockedBuilder);
            when(mockedBuilder.enablePendingPurchases()).thenReturn(mockedBuilder);
            when(mockedBuilder.setListener(any())).thenReturn(mockedBuilder);
            when(mockedBuilder.build()).thenReturn(mockedBillingClient);

            BillingClient billingClient = billingClientFactory.createPendingPurchasesEnabledBillingClient(mockedPurchasesUpdatedListener);

            assertThat(billingClient, sameInstance(mockedBillingClient));
            billingClientMockedStatic.verify(times(1), () -> BillingClient.newBuilder(mockedContext));
            verify(mockedBuilder, times(1)).enablePendingPurchases();
            verify(mockedBuilder, times(1)).setListener(mockedPurchasesUpdatedListener);
            verify(mockedBuilder, times(1)).build();

        }
    }
}
