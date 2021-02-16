package com.abatra.billingr.google;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class GoogleSkuPurchaserTest {

    @Mock
    private InitializedBillingClientSupplier mockedInitializedBillingClientSupplier;

    @InjectMocks
    private GoogleSkuPurchaser googleSkuPurchaser;

    @Test
    public void toDoTest() {
    }
}
