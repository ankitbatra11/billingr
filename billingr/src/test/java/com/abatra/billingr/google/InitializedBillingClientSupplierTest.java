package com.abatra.billingr.google;

import android.os.Build;

import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.SkuPurchase;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class InitializedBillingClientSupplierTest {

    public static final String SKU = "sku";
    public static final String DISCOUNTED_SKU = "discountedSku";
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

    @Captor
    private ArgumentCaptor<PurchasesUpdatedListener> purchasesUpdatedListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<SkuPurchase>> skuPurchasesArgumentCaptor;

    @Mock
    private PurchaseListener mockedPurchaseListenerFirst;

    @Mock
    private PurchaseListener mockedPurchaseListenerSecond;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockedBillingClientFactory.createPendingPurchasesEnabledBillingClient(any(PurchasesUpdatedListener.class)))
                .thenReturn(mockedBillingClient);
    }

    @Test
    public void testGetInitializedBillingClient_billingUnavailable() {

        assertThat(initializedBillingClientSupplier.connecting.get(), equalTo(false));

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);
        verify(mockedBillingClient, times(1)).startConnection(clientStateListenerArgumentCaptor.capture());
        assertThat(initializedBillingClientSupplier.connecting.get(), equalTo(true));

        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE);
        clientStateListenerArgumentCaptor.getValue().onBillingSetupFinished(mockedBillingResult);

        verify(mockedListener, times(1)).onBillingUnavailable();
        verifyNoMoreInteractions(mockedListener);
        assertThat(initializedBillingClientSupplier.connecting.get(), equalTo(false));
    }

    @Test
    public void test_getInitializedBillingClient_startConnectionFails() {

        doThrow(new RuntimeException()).when(mockedBillingClient).startConnection(any());

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);

        verify(mockedListener, times(1)).initializationFailed(any(GoogleBillingrException.class));
    }

    @Test
    public void test_getInitializedBillingClient_alreadyConnecting() {

        assertThat(initializedBillingClientSupplier.connecting.get(), equalTo(false));

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);

        verifyConnectingState();

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);

        verifyConnectingState();
    }

    private void verifyConnectingState() {
        verifyNoInteractions(mockedListener);
        assertThat(initializedBillingClientSupplier.connecting.get(), equalTo(true));
        assertThat(initializedBillingClientSupplier.retriedInitializing.get(), equalTo(false));
    }

    @Test
    public void test_getInitializedBillingClient_retryConnection() {

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);
        verify(mockedBillingClient, times(1)).startConnection(clientStateListenerArgumentCaptor.capture());

        clientStateListenerArgumentCaptor.getValue().onBillingServiceDisconnected();

        verifyNoInteractions(mockedListener);
        assertThat(initializedBillingClientSupplier.retriedInitializing.get(), equalTo(true));
        verify(mockedBillingClient, times(2)).startConnection(clientStateListenerArgumentCaptor.capture());

        clientStateListenerArgumentCaptor.getAllValues().get(1).onBillingServiceDisconnected();

        verify(mockedListener, times(1)).initializationFailed(any(GoogleBillingrException.class));
    }

    @Test
    public void test_getInitializedBillingClient_initialized() {

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);
        verify(mockedBillingClient, times(1)).startConnection(clientStateListenerArgumentCaptor.capture());
        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.OK);

        clientStateListenerArgumentCaptor.getValue().onBillingSetupFinished(mockedBillingResult);

        verify(mockedListener, times(1)).initialized(mockedBillingClient);
        verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void test_getInitializedBillingClient_withError() {

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);
        verify(mockedBillingClient, times(1)).startConnection(clientStateListenerArgumentCaptor.capture());
        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.ERROR);

        clientStateListenerArgumentCaptor.getValue().onBillingSetupFinished(mockedBillingResult);

        verify(mockedListener, times(1)).initializationFailed(any(GoogleBillingrException.class));
    }

    @Test
    public void test_onPurchasesUpdated_successWithPurchases() {

        capturePurchasesUpdatedListener();

        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.OK);
        Purchase skuPurchase = mockPurchase(SKU);
        Purchase discountedSkuPurchase = mockPurchase(DISCOUNTED_SKU);
        purchasesUpdatedListenerArgumentCaptor.getValue().onPurchasesUpdated(mockedBillingResult,
                Arrays.asList(skuPurchase, discountedSkuPurchase));

        verify(mockedPurchaseListenerFirst, times(1)).onPurchasesLoaded(skuPurchasesArgumentCaptor.capture());
        verify(mockedPurchaseListenerSecond, times(1)).onPurchasesLoaded(skuPurchasesArgumentCaptor.capture());

        assertThat(skuPurchasesArgumentCaptor.getAllValues(), hasSize(2));
        verifyUpdatedPurchases(skuPurchasesArgumentCaptor.getAllValues().get(0));
        verifyUpdatedPurchases(skuPurchasesArgumentCaptor.getAllValues().get(1));
    }

    private void capturePurchasesUpdatedListener() {

        initializedBillingClientSupplier.addObserver(mockedPurchaseListenerFirst);
        initializedBillingClientSupplier.addObserver(mockedPurchaseListenerSecond);

        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);

        verify(mockedBillingClientFactory, times(1)).createPendingPurchasesEnabledBillingClient(
                purchasesUpdatedListenerArgumentCaptor.capture());
    }

    private void verifyUpdatedPurchases(List<SkuPurchase> skuPurchases) {
        assertThat(skuPurchases, hasSize(2));
        assertThat(skuPurchases.get(0), instanceOf(GoogleSkuPurchase.class));
        assertThat(skuPurchases.get(0).getSku(), equalTo(SKU));
        assertThat(skuPurchases.get(0).getPurchaseToken(), equalTo(SKU));
        assertThat(skuPurchases.get(1), instanceOf(GoogleSkuPurchase.class));
        assertThat(skuPurchases.get(1).getSku(), equalTo(DISCOUNTED_SKU));
        assertThat(skuPurchases.get(1).getPurchaseToken(), equalTo(DISCOUNTED_SKU));
    }

    private Purchase mockPurchase(String tag) {
        Purchase mockedPurchase = mock(Purchase.class);
        when(mockedPurchase.getSku()).thenReturn(tag);
        when(mockedPurchase.getPurchaseToken()).thenReturn(tag);
        return mockedPurchase;
    }

    @Test
    public void test_onPurchasesUpdated_successWithoutPurchases_null() {

        capturePurchasesUpdatedListener();

        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.OK);
        purchasesUpdatedListenerArgumentCaptor.getValue().onPurchasesUpdated(mockedBillingResult, null);

        verifyNoInteractions(mockedPurchaseListenerFirst, mockedPurchaseListenerSecond);
    }

    @Test
    public void test_onPurchasesUpdated_successWithoutPurchases_emptyList() {

        capturePurchasesUpdatedListener();

        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.OK);
        purchasesUpdatedListenerArgumentCaptor.getValue().onPurchasesUpdated(mockedBillingResult, Collections.emptyList());

        verifyNoInteractions(mockedPurchaseListenerFirst, mockedPurchaseListenerSecond);
    }

    @Test
    public void test_onPurchasesUpdated_failed() {

        capturePurchasesUpdatedListener();

        when(mockedBillingResult.getResponseCode()).thenReturn(BillingClient.BillingResponseCode.ERROR);

        Purchase skuPurchase = mockPurchase(SKU);
        Purchase discountedSkuPurchase = mockPurchase(DISCOUNTED_SKU);
        purchasesUpdatedListenerArgumentCaptor.getValue().onPurchasesUpdated(mockedBillingResult,
                Arrays.asList(skuPurchase, discountedSkuPurchase));

        verifyNoInteractions(mockedPurchaseListenerFirst, mockedPurchaseListenerSecond);
    }

    @Test
    public void test_onDestroy() {

        initializedBillingClientSupplier.addObserver(mock(PurchaseListener.class));
        initializedBillingClientSupplier.addObserver(mock(PurchaseListener.class));
        initializedBillingClientSupplier.getInitializedBillingClient(mockedListener);

        assertThat(initializedBillingClientSupplier.billingClient, notNullValue());

        initializedBillingClientSupplier.onDestroy();

        verify(mockedBillingClient, times(1)).endConnection();
        assertThat(initializedBillingClientSupplier.billingClient, nullValue());
    }
}
