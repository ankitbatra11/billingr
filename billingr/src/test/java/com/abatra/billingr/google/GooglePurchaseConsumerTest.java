package com.abatra.billingr.google;

import com.abatra.android.wheelie.lifecycle.ILifecycleOwner;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.ConsumePurchaseCallback;
import com.abatra.billingr.purchase.ConsumePurchasesCallback;
import com.abatra.billingr.purchase.PurchasesConsumptionResult;
import com.abatra.billingr.purchase.SkuPurchase;
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

import java.util.Arrays;

import static com.abatra.billingr.google.BillingResultMocker.mockBillingResult;
import static com.abatra.billingr.google.GoogleBillingrException.unavailable;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
    private ConsumePurchaseCallback mockedCallback;

    @Captor
    private ArgumentCaptor<BillingrException> billingrExceptionArgumentCaptor;

    @Captor
    private ArgumentCaptor<SkuPurchase> skuPurchaseArgumentCaptor;

    @Mock
    private GoogleSkuPurchase mockedGoogleSkuPurchase;

    @Mock
    private ConsumePurchasesCallback mockedConsumePurchasesCallback;

    @Captor
    private ArgumentCaptor<PurchasesConsumptionResult> purchasesConsumptionResultArgumentCaptor;

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

        verify(mockedCallback, times(1)).onPurchaseConsumptionFailed(skuPurchaseArgumentCaptor.capture(),
                billingrExceptionArgumentCaptor.capture());

        verifySkuPurchase(null);

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), sameInstance(GET_ERROR));
    }

    private void verifySkuPurchase(SkuPurchase skuPurchase) {
        assertThat(skuPurchaseArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(skuPurchaseArgumentCaptor.getValue(), sameInstance(skuPurchase));
    }

    @Test
    public void test_consumePurchases_gettingBillingClientThrowsError() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockGetClientFailure());

        googlePurchaseConsumer.consumePurchases(null, mockedConsumePurchasesCallback);

        verify(mockedConsumePurchasesCallback, times(1))
                .onPurchasesConsumptionFailure(billingrExceptionArgumentCaptor.capture());

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
    public void test_consumePurchases_billingUnavailable() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockUnavailable());

        googlePurchaseConsumer.consumePurchases(null, mockedConsumePurchasesCallback);

        verify(mockedConsumePurchasesCallback, times(1)).onBillingUnavailable();
    }

    @Test
    public void test_consumePurchase_billingInitializationFailed() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockInitializationFailure(unavailable()));

        googlePurchaseConsumer.consumePurchase(null, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumptionFailed(skuPurchaseArgumentCaptor.capture(),
                billingrExceptionArgumentCaptor.capture());

        verifySkuPurchase(null);

        assertThat(billingrExceptionArgumentCaptor.getValue(), sameInstance(unavailable()));
    }

    @Test
    public void test_consumePurchases_billingInitializationFailed() {

        googlePurchaseConsumer = new GooglePurchaseConsumer(mockInitializationFailure(unavailable()));

        googlePurchaseConsumer.consumePurchases(null, mockedConsumePurchasesCallback);

        verify(mockedConsumePurchasesCallback, times(1))
                .onPurchasesConsumptionFailure(billingrExceptionArgumentCaptor.capture());

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue(), sameInstance(unavailable()));
    }

    @Test
    public void test_consumePurchase_initialized_consumeAsyncFails() {

        googlePurchaseConsumer.consumePurchase(mockedGoogleSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumptionFailed(skuPurchaseArgumentCaptor.capture(), billingrExceptionArgumentCaptor.capture());

        verifySkuPurchase(mockedGoogleSkuPurchase);

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause(), notNullValue());
        assertThat(billingrExceptionArgumentCaptor.getValue().getCause().getMessage(), equalTo("Purchase token must be set"));
    }

    @Test
    public void test_consumePurchase_initialized_consumeAsyncReturnsOk() {


        mockPurchaseTokenBillingResponseCode(OK);

        googlePurchaseConsumer.consumePurchase(mockedGoogleSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumed(mockedGoogleSkuPurchase);
    }

    @Test
    public void test_consumePurchase_initialized_consumeAsyncReturnsNotOk() {

        BillingResult billingResult = mockPurchaseTokenBillingResponseCode(ERROR);

        googlePurchaseConsumer.consumePurchase(mockedGoogleSkuPurchase, mockedCallback);

        verify(mockedCallback, times(1)).onPurchaseConsumptionFailed(skuPurchaseArgumentCaptor.capture(),
                billingrExceptionArgumentCaptor.capture());

        verifySkuPurchase(mockedGoogleSkuPurchase);

        assertThat(billingrExceptionArgumentCaptor.getAllValues(), hasSize(1));
        assertThat(billingrExceptionArgumentCaptor.getValue().getMessage(), equalTo(GoogleBillingUtils.toString(billingResult)));
    }

    private BillingResult mockPurchaseTokenBillingResponseCode(int responseCode) {

        when(mockedGoogleSkuPurchase.getPurchaseToken()).thenReturn("some token");

        BillingResult billingResult = mockBillingResult(responseCode);
        doAnswer(invocation ->
        {
            ConsumeResponseListener listener = invocation.getArgument(1);
            listener.onConsumeResponse(billingResult, "some token");
            return null;

        }).when(mockedBillingClient).consumeAsync(any(), any());
        return billingResult;
    }

    @Test
    public void test_consumePurchases_initialized_onePurchaseConsumedOnePurchaseConsumeFailed() {

        mockPurchaseTokenBillingResponseCode(OK);
        GoogleSkuPurchase googleSkuPurchase = mock(GoogleSkuPurchase.class);

        googlePurchaseConsumer.consumePurchases(Arrays.asList(mockedGoogleSkuPurchase, googleSkuPurchase), mockedConsumePurchasesCallback);

        verify(mockedConsumePurchasesCallback, times(3)).onPurchasesConsumptionResultUpdated(purchasesConsumptionResultArgumentCaptor.capture());
        assertThat(purchasesConsumptionResultArgumentCaptor.getAllValues(), hasSize(3));

        assertThat(purchasesConsumptionResultArgumentCaptor.getValue().getConsumedPurchases(), hasSize(1));
        assertThat(purchasesConsumptionResultArgumentCaptor.getValue().getFailedToConsumePurchases(), hasSize(1));
        assertThat(purchasesConsumptionResultArgumentCaptor.getValue().getPurchasesToConsume(), hasSize(2));
        assertThat(purchasesConsumptionResultArgumentCaptor.getValue().isComplete(), equalTo(true));
    }

}
