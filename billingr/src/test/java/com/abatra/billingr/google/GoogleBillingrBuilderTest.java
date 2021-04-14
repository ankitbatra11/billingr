package com.abatra.billingr.google;

import android.content.Context;

import com.abatra.billingr.Billingr;
import com.abatra.billingr.analytics.AnalyticsSkuPurchaser;
import com.abatra.billingr.purchase.SkuPurchaser;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleBillingrBuilderTest {

    @Mock
    private Context mockedContext;

    @InjectMocks
    private GoogleBillingrBuilder googleBillingrBuilder;

    @Mock
    private PurchasesUpdatedListener mockedPurchasesUpdatedListener;

    @Mock
    private BillingClient.Builder mockedBuilder;

    @Mock
    private BillingClient mockedBillingClient;

    @Test
    public void test_build_analyticsDisabled() {
        testBuild(false, false);
    }

    private void testBuild(boolean analyticsEnabled, boolean pendingPurchasesEnabled) {

        googleBillingrBuilder.withAnalyticsEnabled(analyticsEnabled);
        googleBillingrBuilder.withPendingPurchasesEnabled(pendingPurchasesEnabled);

        Billingr billingr = googleBillingrBuilder.build();

        assertThat(billingr, instanceOf(GoogleBillingr.class));
        GoogleBillingr googleBillingr = (GoogleBillingr) billingr;

        assertThat(googleBillingr.availabilityChecker, instanceOf(GoogleBillingAvailabilityChecker.class));
        GoogleBillingAvailabilityChecker googleBillingAvailabilityChecker = (GoogleBillingAvailabilityChecker) googleBillingr.availabilityChecker;
        assertThat(googleBillingAvailabilityChecker.billingClientSupplier, notNullValue());

        assertThat(googleBillingr.purchaseFetcher, instanceOf(GooglePurchaseFetcher.class));
        GooglePurchaseFetcher googlePurchaseFetcher = (GooglePurchaseFetcher) googleBillingr.purchaseFetcher;
        assertThat(googlePurchaseFetcher.billingClientSupplier, sameInstance(googleBillingAvailabilityChecker.billingClientSupplier));

        assertThat(googleBillingr.skuDetailsFetcher, instanceOf(GoogleSkuDetailsFetcher.class));
        GoogleSkuDetailsFetcher googleSkuDetailsFetcher = (GoogleSkuDetailsFetcher) googleBillingr.skuDetailsFetcher;
        assertThat(googleSkuDetailsFetcher.billingClientSupplier, sameInstance(googleBillingAvailabilityChecker.billingClientSupplier));

        if (analyticsEnabled) {
            assertThat(googleBillingr.skuPurchaser, instanceOf(AnalyticsSkuPurchaser.class));
            AnalyticsSkuPurchaser analyticsSkuPurchaser = (AnalyticsSkuPurchaser) googleBillingr.skuPurchaser;
            verifyGoogleSkuPurchaser(googleBillingAvailabilityChecker.billingClientSupplier, analyticsSkuPurchaser.delegate);
        } else {
            verifyGoogleSkuPurchaser(googleBillingAvailabilityChecker.billingClientSupplier, googleBillingr.skuPurchaser);
        }

        try (MockedStatic<BillingClient> billingClientMockedStatic = mockStatic(BillingClient.class)) {

            billingClientMockedStatic.when(() -> BillingClient.newBuilder(mockedContext)).thenReturn(mockedBuilder);
            when(mockedBuilder.enablePendingPurchases()).thenReturn(mockedBuilder);
            when(mockedBuilder.setListener(any())).thenReturn(mockedBuilder);
            when(mockedBuilder.build()).thenReturn(mockedBillingClient);

            googlePurchaseFetcher.billingClientSupplier.billingClientFactory.apply(mockedPurchasesUpdatedListener);

            billingClientMockedStatic.verify(times(1), () -> BillingClient.newBuilder(mockedContext));
            verify(mockedBuilder, times(1)).setListener(mockedPurchasesUpdatedListener);
            verify(mockedBuilder, times(1)).build();
            if (pendingPurchasesEnabled) {
                verify(mockedBuilder, times(1)).enablePendingPurchases();
            } else {
                verify(mockedBuilder, never()).enablePendingPurchases();
            }
        }
    }

    private void verifyGoogleSkuPurchaser(InitializedBillingClientSupplier billingClientSupplier, SkuPurchaser delegate) {
        assertThat(delegate, instanceOf(GoogleSkuPurchaser.class));
        GoogleSkuPurchaser googleSkuPurchaser = (GoogleSkuPurchaser) delegate;
        assertThat(googleSkuPurchaser.billingClientSupplier, sameInstance(billingClientSupplier));
    }

    @Test
    public void test_build_analyticsEnabled() {
        testBuild(true, false);
    }

    @Test
    public void test_build_pendingPurchasesEnabled() {
        testBuild(false, true);
    }
}
