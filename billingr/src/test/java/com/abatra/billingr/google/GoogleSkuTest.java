package com.abatra.billingr.google;

import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuType;
import com.android.billingclient.api.SkuDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleSkuTest {

    @Mock
    private SkuDetails mockedSkuDetails;

    private GoogleSku googleSku;

    @Before
    public void setup() {
        googleSku = new GoogleSku(SkuType.IN_APP_PRODUCT, mockedSkuDetails);
    }

    @Test
    public void test_getSkuDetails() {
        assertThat(googleSku.getSkuDetails(), sameInstance(mockedSkuDetails));
    }

    @Test
    public void test_getId() {

        assertThat(googleSku.getId(), equalTo(mockedSkuDetails.getSku()));

        verify(mockedSkuDetails, times(2)).getSku();
    }

    @Test
    public void test_getType() {
        assertThat(googleSku.getType(), sameInstance(SkuType.IN_APP_PRODUCT));
    }

    @Test
    public void test_getTitle() {

        assertThat(googleSku.getTitle(), equalTo(mockedSkuDetails.getTitle()));

        verify(mockedSkuDetails, times(2)).getTitle();
    }

    @Test
    public void test_getCurrency() {

        assertThat(googleSku.getCurrency(), equalTo(mockedSkuDetails.getPriceCurrencyCode()));

        verify(mockedSkuDetails, times(2)).getPriceCurrencyCode();
    }

    @Test
    public void test_getPriceAmount() {

        when(mockedSkuDetails.getPriceAmountMicros()).thenReturn(1000000L);

        assertThat(googleSku.getPriceAmount(), equalTo(1.0));
    }

    @Test
    public void test_getPriceAmountMicros() {

        assertThat(googleSku.getPriceAmountMicros(), equalTo(mockedSkuDetails.getPriceAmountMicros()));

        verify(mockedSkuDetails, times(2)).getPriceAmountMicros();
    }

    @Test
    public void test_getOriginalJson() {

        assertThat(googleSku.getOriginalJson(), equalTo(mockedSkuDetails.getOriginalJson()));

        verify(mockedSkuDetails, times(2)).getOriginalJson();
    }

    @Test
    public void test_getAffiliation() {
        assertThat(googleSku.getAffiliation(), sameInstance(Sku.AFFILIATION_GOOGLE));
    }

    @Test
    public void test_toString() {
        try (MockedStatic<Sku> skuMockedStatic = mockStatic(Sku.class)) {
            assertThat(googleSku.toString(), nullValue());
            skuMockedStatic.verify(timeout(1), () -> Sku.toString(googleSku));
        }
    }

}
