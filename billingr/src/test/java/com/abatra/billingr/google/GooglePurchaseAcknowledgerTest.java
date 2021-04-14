package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.AcknowledgePurchaseCallback;
import com.abatra.billingr.purchase.AcknowledgePurchasesCallback;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static com.abatra.billingr.google.BillingResultMocker.mockBillingResult;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitializationFailure;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockInitialized;
import static com.abatra.billingr.google.InitializedBillingClientSupplierMocker.mockUnavailable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.same;
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
    private AcknowledgePurchaseCallback mockedCallback;

    @Captor
    private ArgumentCaptor<BillingrException> billingrExceptionArgumentCaptor;

    @Mock
    private GoogleSkuPurchase mockedSkuPurchase;

    @Mock
    private Purchase mockedPurchase;

    @Captor
    private ArgumentCaptor<SkuPurchase> skuPurchaseArgumentCaptor;

    @Mock
    private AcknowledgePurchasesCallback mockedAcknowledgePurchasesCallback;

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

    private void verifyCapturedSkuPurchase(SkuPurchase skuPurchase) {
        assertThat(skuPurchaseArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(skuPurchaseArgumentCaptor.getValue(), sameInstance(skuPurchase));
    }

    @Test
    public void test_acknowledgePurchase_billingUnavailable() {

        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockUnavailable());

        googlePurchaseAcknowledger.acknowledgePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onBillingUnavailable();
    }

    @Test
    public void test_acknowledgePurchases_billingUnavailable() {

        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockUnavailable());

        googlePurchaseAcknowledger.acknowledgePurchases(Collections.emptyList(), mockedAcknowledgePurchasesCallback);

        verify(mockedAcknowledgePurchasesCallback, times(1)).onBillingUnavailable();
    }

    @Test
    public void test_acknowledgePurchase_billingClientError() {

        BillingrException error = new GoogleBillingrException("Simulate failure!");
        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockInitializationFailure(error));

        googlePurchaseAcknowledger.acknowledgePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(null, error);

    }

    @Test
    public void test_acknowledgePurchases_billingClientError() {

        BillingrException error = new GoogleBillingrException("Simulate failure!");
        googlePurchaseAcknowledger = new GooglePurchaseAcknowledger(mockInitializationFailure(error));

        googlePurchaseAcknowledger.acknowledgePurchases(Collections.emptyList(), mockedAcknowledgePurchasesCallback);

        verify(mockedAcknowledgePurchasesCallback, times(1)).onPurchaseAcknowledgeProcessFailure(error);
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchaseAlreadyAcknowledged() {

        when(mockedPurchase.isAcknowledged()).thenReturn(true);

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledged(mockedSkuPurchase);
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePending() {

        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PENDING);

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(skuPurchaseArgumentCaptor.capture(),
                billingrExceptionArgumentCaptor.capture());

        verifyCapturedSkuPurchase(mockedSkuPurchase);

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), Matchers.startsWith("Purchase"));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), Matchers.endsWith("has not been purchased yet!"));
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePurchased_billingClientAckPurchaseFails() {

        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PURCHASED);

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(skuPurchaseArgumentCaptor.capture(),
                billingrExceptionArgumentCaptor.capture());

        verifyCapturedSkuPurchase(mockedSkuPurchase);

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), notNullValue());
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause().getMessage(), equalTo("Purchase token must be set"));
    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePurchased_billingClientAckPurchaseReturnsOk() {

        mockPurchasedState();

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledged(mockedSkuPurchase);

    }

    @Test
    public void test_acknowledgePurchase_gotBillingClient_purchasePurchased_billingClientAckPurchaseReturnsNotOk() {

        mockPurchasedState();

        BillingResult billingResult = mockBillingResult(BillingClient.BillingResponseCode.ERROR);
        doAnswer(invocation ->
        {
            AcknowledgePurchaseResponseListener listener = invocation.getArgument(1);
            listener.onAcknowledgePurchaseResponse(billingResult);
            return null;

        }).when(mockedBillingClient).acknowledgePurchase(any(), any());

        googlePurchaseAcknowledger.acknowledgePurchase(mockedSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseAcknowledgeFailed(skuPurchaseArgumentCaptor.capture(),
                billingrExceptionArgumentCaptor.capture());

        verifyCapturedSkuPurchase(mockedSkuPurchase);

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(),
                equalTo(GoogleBillingUtils.toString(billingResult)));
    }

    private void mockPurchasedState() {
        when(mockedPurchase.isAcknowledged()).thenReturn(false);
        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PURCHASED);
        when(mockedSkuPurchase.getPurchaseToken()).thenReturn("some token");
    }

    @Test
    public void test_acknowledgePurchases_onePurchaseAcknowledgedOnePurchaseAcknowledgementFails() {

        mockPurchasedState();

        SkuPurchase pendingSkuPurchase = mock(SkuPurchase.class);

        googlePurchaseAcknowledger.acknowledgePurchases(Arrays.asList(mockedSkuPurchase, pendingSkuPurchase),
                mockedAcknowledgePurchasesCallback);

        verify(mockedAcknowledgePurchasesCallback, times(1)).onPurchaseAcknowledged(mockedSkuPurchase);
        verify(mockedAcknowledgePurchasesCallback, times(1)).onPurchaseAcknowledgeFailed(same(pendingSkuPurchase),
                any(BillingrException.class));
    }

    @Test
    public void test_observeLifecycle() {

        ILifecycleOwner mockedLifecycleOwner = mock(ILifecycleOwner.class);

        googlePurchaseAcknowledger.observeLifecycle(mockedLifecycleOwner);

        verify(googlePurchaseAcknowledger.billingClientSupplier, times(1)).observeLifecycle(mockedLifecycleOwner);
    }
}
