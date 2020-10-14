package com.abatra.billingr.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.abatra.billingr.Billingr;
import com.abatra.billingr.load.LoadBillingRequest;

public class BillingDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Billingr billingr = Billingr.google(getApplicationContext());
        getLifecycle().addObserver(billingr);
        billingr.loadBilling(LoadBillingRequest.builder().build());
    }
}
