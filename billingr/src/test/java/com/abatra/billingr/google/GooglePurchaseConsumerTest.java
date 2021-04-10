package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseConsumer;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.abatra.billingr.BillingrException.unavailable;
import static com.abatra.billingr.google.BillingResultMocker.mockBillingResult;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.GET_ERROR;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockGetClientFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitializationFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitialized;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockUnavailable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GooglePurchaseConsumerTest {

    private GooglePurchaseConsumer googlePurchaseConsumer;

    @Mock
    private BillingClient mockedBillingClient;

    @Mock
    private ILifecycleOwner mockedLifecycleOwner;

    private InitializedBillingClientSupplier mockedBillingClientSupplier;

    @Mock
    private PurchaseConsumer.Callback mockedCallback;

    @Captor
    private ArgumentCaptor<BillingrException> billingrExceptionArgumentCaptor;

    @Mock
    private GoogleSkuPurchase mockedGoogleSkuPurchase;

    @Before
    public void setup() {
        mockedBillingClientSupplier = mockInitialized(mockedBillingClient);
        googlePurchaseConsumer = new GooglePurchaseConsumer(mockedBillingClientSupplier);
    }

    @Test
    public void test_observeLifecycle() {

        googlePurchaseConsumer.observeLifecycle(mockedLifecycleOwner);

        verify(mockedBillingClientSupplier, times(1)).observeLifecycle(mockedLifecycleOwner);
    }

    @Test
    public void test_consumePurchase_gettingBillingClientThrowsError() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockGetClientFailure());

        googlePurchaseConsumer.consumePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), sameInstance(GET_ERROR));
    }

    @Test
    public void test_consumePurchase_billingUnavailable() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockUnavailable());

        googlePurchaseConsumer.consumePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onBillingUnavailable();
    }

    @Test
    public void test_consumePurchase_billingInitializationFailed() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockInitializationFailure(unavailable()));

        googlePurchaseConsumer.consumePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), sameInstance(unavailable()));
    }

    @Test
    public void test_consumePurchase_initialized_consumeAsyncFails() {

        googlePurchaseConsumer.consumePurchase(mockedGoogleSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), notNullValue());
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause().getMessage(), equalTo("Purchase token must be set"));
    }

    @Test
    public void test_consumePurchase_initialized_consumeAsyncReturnsOk() {

        doAnswer(invocation ->
        {
            ConsumeResponseListener listener = invocation.getArgument(1);
            listener.onConsumeResponse(mockBillingResult(BillingClient.BillingResponseCode.OK), "some token");
            return null;

        }).when(mockedBillingClient).consumeAsync(any(), any());

        when(mockedGoogleSkuPurchase.getPurchaseToken()).thenReturn("some token");

        googlePurchaseConsumer.consumePurchase(mockedGoogleSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumed();
    }

    @Test
    public void test_consumePurchase_initialized_consumeAsyncReturnsNotOk() {

        BillingResult billingResult = mockBillingResult(BillingClient.BillingResponseCode.ERROR);
        doAnswer(invocation ->
        {
            ConsumeResponseListener listener = invocation.getArgument(1);
            listener.onConsumeResponse(billingResult, "some token");
            return null;

        }).when(mockedBillingClient).consumeAsync(any(), any());

        when(mockedGoogleSkuPurchase.getPurchaseToken()).thenReturn("some token");

        googlePurchaseConsumer.consumePurchase(mockedGoogleSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), equalTo(GoogleBillingUtils.toString(billingResult)));
    }

}
