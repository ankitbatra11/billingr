package com.abatra.billingr.google;

import com.android.billingclient.api.Purchase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleSkuPurchaseTest {

    @InjectMocks
    private GoogleSkuPurchase googleSkuPurchase;

    @Mock
    private Purchase purchase;

    @Test
    public void test_getSku() {

        assertThat(googleSkuPurchase.getSku(), sameInstance(purchase.getSku()));

        verify(purchase, times(2)).getSku();

    }

    @Test
    public void test_getPurchaseToken() {

        assertThat(googleSkuPurchase.getPurchaseToken(), sameInstance(purchase.getPurchaseToken()));

        verify(purchase, times(2)).getPurchaseToken();
    }

    @Test
    public void test_isAcknowledged() {

        when(purchase.isAcknowledged()).thenReturn(true);

        assertThat(googleSkuPurchase.isAcknowledged(), equalTo(true));

        verify(purchase, times(1)).isAcknowledged();
    }
}
