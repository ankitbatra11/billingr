package com.abatra.billingr.google;

import com.abatra.billingr.BillingrException;
import com.abatra.billingr.purchase.SkuPurchase;
import com.abatra.billingr.sku.SkuType;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.abatra.billingr.google.BillingResultMocker.mockBillingResult;
import static com.abatra.billingr.google.GoogleBillingUtils.getSkuType;
import static com.abatra.billingr.google.GoogleBillingUtils.isError;
import static com.abatra.billingr.google.GoogleBillingUtils.isOk;
import static com.abatra.billingr.google.GoogleBillingUtils.isPurchased;
import static com.abatra.billingr.google.GoogleBillingUtils.isUnavailable;
import static com.abatra.billingr.google.GoogleBillingUtils.removeAppName;
import static com.abatra.billingr.google.GoogleBillingUtils.reportErrorAndGet;
import static com.abatra.billingr.google.GoogleBillingUtils.toSkuPurchases;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.BILLING_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.DEVELOPER_ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleBillingUtilsTest {

    @Mock
    private BillingResult mockedBillingResult;

    @Mock
    private Purchase mockedPurchase;

    @Test
    public void test_getSkuType_inApp() {
        assertThat(getSkuType(SkuType.IN_APP_PRODUCT), equalTo(BillingClient.SkuType.INAPP));
    }

    @Test
    public void test_getSkuType_subs() {
        assertThat(getSkuType(SkuType.SUBSCRIPTION), equalTo(BillingClient.SkuType.SUBS));
    }

    @Test
    public void test_removeAppName_nameInPara() {
        assertThat(removeAppName("Cool (AB)"), equalTo("Cool"));
    }

    @Test
    public void test_removeAppName_nameWithJustOpeningPara() {
        assertThat(removeAppName("Cool (AB"), equalTo("Cool"));
    }

    @Test
    public void test_removeAppName_nameInJustClosingPara() {
        assertThat(removeAppName("Cool AB)"), equalTo("Cool AB)"));
    }

    @Test
    public void test_removeAppName_nameWithoutPara() {
        assertThat(removeAppName("CAB"), equalTo("CAB"));
    }

    @Test
    public void test_removeAppName_null() {
        assertThat(removeAppName(null), equalTo(""));
    }

    @Test
    public void test_removeAppName_empty() {
        assertThat(removeAppName(""), equalTo(""));
    }

    @Test
    public void test_IsOk_ok() {

        when(mockedBillingResult.getResponseCode()).thenReturn(OK);

        assertThat(isOk(mockedBillingResult), equalTo(true));
    }

    @Test
    public void test_IsOk_notOk() {

        when(mockedBillingResult.getResponseCode()).thenReturn(ERROR);

        assertThat(isOk(mockedBillingResult), equalTo(false));
    }

    @Test
    public void test_isPurchased_purchased() {

        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PURCHASED);

        assertThat(isPurchased(mockedPurchase), equalTo(true));
    }

    @Test
    public void test_isPurchased_notPurchased() {

        when(mockedPurchase.getPurchaseState()).thenReturn(Purchase.PurchaseState.PENDING);

        assertThat(isPurchased(mockedPurchase), equalTo(false));
    }

    @Test
    public void test_isError_error() {
        assertThat(isError(mockBillingResult(ERROR)), equalTo(true));
    }

    @Test
    public void test_isError_developerError() {
        assertThat(isError(mockBillingResult(DEVELOPER_ERROR)), equalTo(true));
    }

    @Test
    public void test_isError_noError() {
        assertThat(isError(mockBillingResult(BILLING_UNAVAILABLE)), equalTo(false));
    }

    @Test
    public void test_isUnavailable_true() {
        assertThat(isUnavailable(mockBillingResult(BILLING_UNAVAILABLE)), equalTo(true));
    }

    @Test
    public void test_isUnavailable_false() {
        assertThat(isUnavailable(mockBillingResult(OK)), equalTo(false));
    }

    @Test
    public void test_reportErrorAndGet() {

        BillingResult billingResult = mockBillingResult();

        BillingrException billingrException = reportErrorAndGet(billingResult, "error=%d", 1);

        assertThat(billingrException.getMessage(), equalTo(GoogleBillingUtils.toString(billingResult)));
    }

    @Test
    public void test_toSkuPurchases_null() {
        assertThat(toSkuPurchases(null), hasSize(0));

    }

    @Test
    public void test_toSkuPurchases_empty() {
        assertThat(toSkuPurchases(Collections.emptyList()), hasSize(0));
    }

    @Test
    public void test_toSkuPurchases_moreThan1() {

        List<Purchase> purchases = Arrays.asList(mockedPurchase, mock(Purchase.class));

        List<SkuPurchase> skuPurchases = toSkuPurchases(purchases);

        assertThat(skuPurchases, hasSize(2));
        for (int i = 0; i < 2; i++) {
            assertThat(skuPurchases.get(i), instanceOf(GoogleSkuPurchase.class));
            GoogleSkuPurchase purchase = (GoogleSkuPurchase) skuPurchases.get(i);
            assertThat(purchase.getPurchase(), sameInstance(purchases.get(i)));
        }
    }

}
