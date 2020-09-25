package com.abatra.billingr.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.abatra.billingr.BillingUseCase;
import com.abatra.billingr.LoadBillingRequest;

public class BillingDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BillingUseCase billingUseCase = BillingUseCase.google(getApplicationContext());
        getLifecycle().addObserver(billingUseCase);
        billingUseCase.loadBilling(LoadBillingRequest.builder().build());
    }
}
