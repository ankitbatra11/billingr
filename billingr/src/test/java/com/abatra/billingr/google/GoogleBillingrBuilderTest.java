package com.abatra.billingr.google;

import android.content.Context;

import com.abatra.billingr.Billingr;
import com.abatra.billingr.analytics.AnalyticsSkuPurchaser;
import com.abatra.billingr.purchase.SkuPurchaser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;

@RunWith(MockitoJUnitRunner.class)
public class GoogleBillingrBuilderTest {

    @Mock
    private Context mockedContext;

    @InjectMocks
    private GoogleBillingrBuilder googleBillingrBuilder;

    @Test
    public void test_build_analyticsDisabled() {

        testBuild(false);
    }

    private void testBuild(boolean analyticsEnabled) {

        googleBillingrBuilder.setAnalyticsEnabled(analyticsEnabled);

        Billingr billingr = googleBillingrBuilder.build();

        assertThat(billingr, instanceOf(GoogleBillingr.class));
        GoogleBillingr googleBillingr = (GoogleBillingr) billingr;

        assertThat(googleBillingr.availabilityChecker, instanceOf(GoogleBillingAvailabilityChecker.class));
        GoogleBillingAvailabilityChecker googleBillingAvailabilityChecker = (GoogleBillingAvailabilityChecker) googleBillingr.availabilityChecker;
        assertThat(googleBillingAvailabilityChecker.billingClientSupplier, sameInstance(googleBillingr.billingClientSupplier));

        assertThat(googleBillingr.purchaseFetcher, instanceOf(GooglePurchaseFetcher.class));
        GooglePurchaseFetcher googlePurchaseFetcher = (GooglePurchaseFetcher) googleBillingr.purchaseFetcher;
        assertThat(googlePurchaseFetcher.billingClientSupplier, sameInstance(googleBillingr.billingClientSupplier));

        assertThat(googleBillingr.skuDetailsFetcher, instanceOf(GoogleSkuDetailsFetcher.class));
        GoogleSkuDetailsFetcher googleSkuDetailsFetcher = (GoogleSkuDetailsFetcher) googleBillingr.skuDetailsFetcher;
        assertThat(googleSkuDetailsFetcher.billingClientSupplier, sameInstance(googleBillingr.billingClientSupplier));

        if (analyticsEnabled) {
            assertThat(googleBillingr.skuPurchaser, instanceOf(AnalyticsSkuPurchaser.class));
            AnalyticsSkuPurchaser analyticsSkuPurchaser = (AnalyticsSkuPurchaser) googleBillingr.skuPurchaser;
            verifyGoogleSkuPurchaser(googleBillingr, analyticsSkuPurchaser.getDelegate());
        } else {
            verifyGoogleSkuPurchaser(googleBillingr, googleBillingr.skuPurchaser);
        }
    }

    private void verifyGoogleSkuPurchaser(GoogleBillingr googleBillingr, SkuPurchaser delegate) {
        assertThat(delegate, instanceOf(GoogleSkuPurchaser.class));
        GoogleSkuPurchaser googleSkuPurchaser = (GoogleSkuPurchaser) delegate;
        assertThat(googleSkuPurchaser.billingClientSupplier, sameInstance(googleBillingr.billingClientSupplier));
    }

    @Test
    public void test_build_analyticsEnabled() {

        testBuild(true);
    }
}
