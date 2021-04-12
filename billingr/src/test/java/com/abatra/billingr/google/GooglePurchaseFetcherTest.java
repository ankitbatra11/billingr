package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static com.abatra.billingr.BillingrException.unavailable;
import static com.abatra.billingr.google.BillingResultMocker.mockBillingResult;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.GET_ERROR;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockGetClientFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitializationFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitialized;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockUnavailable;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GooglePurchaseFetcherTest {

    @InjectMocks
    private GooglePurchaseFetcher googlePurchaseFetcher;

    @Mock
    private PurchaseListener mockedPurchaseListener;

    @Mock
    private BillingClient mockedBillingClient;

    @Captor
    private ArgumentCaptor<BillingrException> billingrExceptionArgumentCaptor;

    @Mock
    private Purchase.PurchasesResult mockedPurchasesResult;

    @Captor
    private ArgumentCaptor<List<SkuPurchase>> skuPurchasesArgumentCaptor;

    @Before
    public void setup() {
        googlePurchaseFetcher = new GooglePurchaseFetcher(mockInitialized(mockedBillingClient));
    }

    @Test
    public void testFetchInAppPurchases_billingUnavailable() {

        googlePurchaseFetcher = new GooglePurchaseFetcher(mockUnavailable());

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onBillingUnavailable();
        verifyNoMoreInteractions(mockedPurchaseListener);
    }

    @Test
    public void test_fetchInAppPurchases_getBillingClientFails() {

        googlePurchaseFetcher = new GooglePurchaseFetcher(mockGetClientFailure());

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onPurchasesLoadFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), sameInstance(GET_ERROR));
    }

    @Test
    public void test_fetchInAppPurchases_clientInitializeFails() {

        googlePurchaseFetcher = new GooglePurchaseFetcher(mockInitializationFailure(unavailable()));

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onPurchasesLoadFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue(), sameInstance(unavailable()));
    }

    @Test
    public void test_fetchInAppPurchases_billingClientInitialized_queryPurchasesFailed() {

        RuntimeException runtimeException = new RuntimeException();
        when(mockedBillingClient.queryPurchases(any())).thenThrow(runtimeException);

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onPurchasesLoadFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), sameInstance(runtimeException));
    }

    @Test
    public void test_fetchInAppPurchases_billingClientInitialized_queryPurchasesBillingResultNotOk() {

        BillingResult billingResult = mockBillingResult(ERROR);
        when(mockedPurchasesResult.getBillingResult()).thenReturn(billingResult);
        when(mockedBillingClient.queryPurchases(any())).thenReturn(mockedPurchasesResult);

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onPurchasesLoadFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), equalTo(GoogleBillingUtils.toString(billingResult)));
    }

    @Test
    public void test_fetchInAppPurchases_billingClientInitialized_billingResultOk_nullPurchases() {

        BillingResult billingResult = mockBillingResult(OK);
        when(mockedPurchasesResult.getBillingResult()).thenReturn(billingResult);
        when(mockedBillingClient.queryPurchases(any())).thenReturn(mockedPurchasesResult);

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onPurchasesLoaded(Collections.emptyList());
    }

    @Test
    public void test_fetchInAppPurchases_billingClientInitialized_billingResultOk_emptyPurchases() {

        BillingResult billingResult = mockBillingResult(OK);
        when(mockedPurchasesResult.getBillingResult()).thenReturn(billingResult);
        when(mockedBillingClient.queryPurchases(any())).thenReturn(mockedPurchasesResult);
        when(mockedPurchasesResult.getPurchasesList()).thenReturn(Collections.emptyList());

        googlePurchaseFetcher.fetchInAppPurchases(mockedPurchaseListener);

        verify(mockedPurchaseListener, times(1)).onPurchasesLoaded(Collections.emptyList());
    }

    @Test
    public void test_observeLifecycle() {

        ILifecycleOwner lifecycleOwner = mock(ILifecycleOwner.class);

        googlePurchaseFetcher.observeLifecycle(lifecycleOwner);

        verify(googlePurchaseFetcher.billingClientSupplier, times(1)).observeLifecycle(lifecycleOwner);
    }
}
