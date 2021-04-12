package com.abatra.billingr.google;

import com.android.billingclient.api.BillingResult;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class BillingResultMocker {

    public static final String DEBUG_MESSAGE = "debug message";
    public static final int RESPONSE_CODE = 1;

    public static BillingResult mockBillingResult() {
        return mockBillingResult(RESPONSE_CODE);
    }

    public static BillingResult mockBillingResult(int responseCode) {
        BillingResult billingResult = mock(BillingResult.class);
        when(billingResult.getResponseCode()).thenReturn(responseCode);
        when(billingResult.getDebugMessage()).thenReturn(DEBUG_MESSAGE);
        return billingResult;
    }
}
