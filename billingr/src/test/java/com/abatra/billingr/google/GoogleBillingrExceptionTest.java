package com.abatra.billingr.google;

import com.android.billingclient.api.BillingResult;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GoogleBillingrExceptionTest {

    @Test
    public void test_from() {

        BillingResult billingResult = BillingResultMocker.mockBillingResult();

        GoogleBillingrException exception = GoogleBillingrException.from(billingResult);

        assertThat(exception.getMessage(), equalTo(GoogleBillingUtils.toString(billingResult)));

    }
}
