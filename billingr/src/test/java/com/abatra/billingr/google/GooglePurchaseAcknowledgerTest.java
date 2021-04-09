package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.PurchaseAcknowledger;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.abatra.billingr.google.BillingResultMocker.mockBillingResult;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.GET_ERROR;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockGetClientFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitializationFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitialized;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockUnavailable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GooglePurchaseAcknowledgerTest {

    private GooglePurchaseAcknowledger googlePurchaseAcknowledger;

    @Mock
    private BillingClient mockedBillingClient;

    @Mock
    private PurchaseAcknowledger.Callback mockedCallback;

    @Captor
    private ArgumentCaptor<BillingrException> billingrExceptionArgumentCaptor;

    @Mock
    private GoogleSkuPurchase mockedSkuPurchase;

    @Mock
    private Purchase mockedPurchase;

    @Before
    public void setup() {

        when(mockedSkuPurchase.getPurchase()).thenReturn(mockedPurchase);

        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockInitialized(mockedBillingClient));

        doAnswer(invocation ->
        {
            AcknowledgePurchaseResponseListener listener = invocation.getArgument(1);
            listener.onAcknowledgePurchaseResponse(mockBillingResult(BillingClient.BillingResponseCode.OK));
            return null;

        }).when(mockedBillingClient).acknowledgePurchase(any(), any());
    }

    @Test
    public void test_acknowledgePurchase_getOnBillingClientFails() {


        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockGetClientFailure());

        googlePurchaseAcknowledger.acknowledgePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), sameInstance(GET_ERROR));
    }

    @Test
    public void test_acknowledgePurchase_billingUnavailable() {

        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockUnavailable());

        googlePurchaseAcknowledger.acknowledgePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onBillingUnavailable();
    }

    @Test
    public void test_acknowledgePurchase_billingClientError() {

        BillingrException error = new BillingrException("Simulate failure!");
        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockInitializationFailure(error));

        googlePurchaseAcknowledger.acknowledgePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(error);

    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchaseAlreadyAcknowledged() {

        when(mockedPurchase.isAcknowledged()).thenReturn(true);

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledged();
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePending() {

        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PENDING);

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), Matchers.startsWith("Purchase"));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), Matchers.endsWith("has not been purchased yet!"));
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePurchased_billingClientAckPurchaseFails() {

        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PURCHASED);

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause().getMessage(), equalTo("Purchase token must be set"));
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePurchased_billingClientAckPurchaseReturnsOk() {

        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PURCHASED);
        when(mockedSkuPurchase.getPurchaseToken()).thenReturn("some token");

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledged();

    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePurchased_billingClientAckPurchaseReturnsNotOk() {

        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PURCHASED);
        when(mockedSkuPurchase.getPurchaseToken()).thenReturn("some token");

        BillingResult billingResult = mockBillingResult(BillingClient.BillingResponseCode.ERROR);
        doAnswer(invocation ->
        {
            AcknowledgePurchaseResponseListener listener = invocation.getArgument(1);
            listener.onAcknowledgePurchaseResponse(billingResult);
            return null;

        }).when(mockedBillingClient).acknowledgePurchase(any(), any());

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(billingrExceptionArgumentCaptor.capture());
        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(),
                equalTo(GoogleBillingUtils.toString(billingResult)));
    }

    @Test
    public void test_observeLifecycle() {

        ILifecycleOwner mockedLifecycleOwner = mock(ILifecycleOwner.class);

        googlePurchaseAcknowledger.observeLifecycle(mockedLifecycleOwner);

        verify(googlePurchaseAcknowledger.billingClientSupplier, times(1)).observeLifecycle(mockedLifecycleOwner);
    }
}
