package com.abatra.billingr.analytics;

import android.app.Activity;
import android.os.Build;

import com.abatra.android.wheelie.chronicle.Chronicle;
import com.abatra.android.wheelie.chronicle.model.BeginCheckoutEventParams;
import com.abatra.android.wheelie.chronicle.model.PurchaseEventParams;
import com.abatra.billingr.Sku;
import com.abatra.billingr.SkuPurchase;
import com.abatra.billingr.SkuPurchaser;
import com.abatra.billingr.SkuType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class AnalyticsSkuPurchaserTest {

    private static final String SKU_ID = "skuId";
    private static final double VALUE = 3.99;
    private static final String CURRENCY = "INR";

    @InjectMocks
    private AnalyticsSkuPurchaser analyticsSkuPurchaser;

    @Mock
    private SkuPurchaser mockedSkuPurchaser;

    @Mock
    private Sku mockedSku;

    @Mock
    private Activity mockedActivity;

    @Mock
    private SkuPurchaser.Listener mockedListener;

    @Mock
    private SkuPurchase mockedSkuPurchase;

    private MockedStatic<Chronicle> chronicleMockedStatic;

    @Captor
    private ArgumentCaptor<BeginCheckoutEventParams> checkoutEventParamsArgumentCaptor;

    @Captor
    private ArgumentCaptor<PurchaseEventParams> purchaseEventParamsArgumentCaptor;

    @Before
    public void setup() {

        MockitoAnnotations.openMocks(this);

        doAnswer(invocation ->
        {
            SkuPurchaser.Listener listener = invocation.getArgument(2);
            listener.purchaseFlowLaunchedSuccessfully();
            return null;

        }).when(mockedSkuPurchaser).launchPurchaseFlow(any(), any(), any());

        when(mockedSku.getId()).thenReturn(SKU_ID);
        when(mockedSku.getType()).thenReturn(SkuType.IN_APP_PRODUCT);
        when(mockedSku.getPriceAmount()).thenReturn(VALUE);
        when(mockedSku.getCurrency()).thenReturn(CURRENCY);

        when(mockedSkuPurchase.getSku()).thenReturn(SKU_ID);

        chronicleMockedStatic = mockStatic(Chronicle.class);
    }

    @After
    public void tearDown() {
        chronicleMockedStatic.close();
    }

    @Test
    public void test_launchPurchaseFlow() {

        analyticsSkuPurchaser.launchPurchaseFlow(mockedSku, mockedActivity, mockedListener);

        verify(mockedListener, times(1)).purchaseFlowLaunchedSuccessfully();

        chronicleMockedStatic.verify(times(1), () -> Chronicle.recordBeginCheckoutEvent(checkoutEventParamsArgumentCaptor.capture()));
        assertThat(checkoutEventParamsArgumentCaptor.getValue(), instanceOf(BeginCheckoutEventParams.class));
        BeginCheckoutEventParams params = checkoutEventParamsArgumentCaptor.getValue();
        assertThat(params.getPrice().getValue(), equalTo(VALUE));
        assertThat(params.getPrice().getCurrency(), equalTo(CURRENCY));

        verify(mockedSku, times(1)).getId();
        verify(mockedSku, times(1)).getTitle();
        verify(mockedSku, times(1)).getType();
        verify(mockedSku, times(1)).getCurrency();
        verify(mockedSku, times(2)).getPriceAmount();
    }

    @Test
    public void test_updated() {

        analyticsSkuPurchaser.checkedOutSku = mockedSku;

        analyticsSkuPurchaser.updated(Collections.singletonList(mockedSkuPurchase));

        chronicleMockedStatic.verify(times(1), () -> Chronicle.recordPurchaseEvent(purchaseEventParamsArgumentCaptor.capture()));
        assertThat(purchaseEventParamsArgumentCaptor.getValue(), instanceOf(PurchaseEventParams.class));
        PurchaseEventParams params = purchaseEventParamsArgumentCaptor.getValue();
        assertThat(params.getPrice().getValue(), equalTo(VALUE));
        assertThat(params.getPrice().getCurrency(), equalTo(CURRENCY));

        verify(mockedSku, times(1)).getAffiliation();
        verify(mockedSku, times(2)).getId();
        verify(mockedSku, times(1)).getTitle();
        verify(mockedSku, times(1)).getType();
        verify(mockedSku, times(1)).getCurrency();
        verify(mockedSku, times(2)).getPriceAmount();

        verify(mockedSkuPurchase, times(1)).getPurchaseToken();
        verify(mockedSkuPurchase, times(1)).getSku();
    }
}
