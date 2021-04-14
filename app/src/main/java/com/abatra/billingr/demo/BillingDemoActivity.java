package com.abatra.billingr.demo;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.abatra.billingr.BillingrBuilder;
import com.abatra.billingr.BillingrException;
import com.abatra.billingr.demo.databinding.BillingrDemoActivityBinding;
import com.abatra.billingr.purchase.PurchaseListener;
import com.abatra.billingr.purchase.SkuPurchase;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class BillingDemoActivity extends AppCompatActivity {

    private BillingrDemoActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = BillingrDemoActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        BillingrBuilder billingrBuilder = BillingrBuilder.google(this);
        billingrBuilder.withAnalyticsEnabled(true);
        billingrBuilder.build().fetchInAppPurchases(new PurchaseListener() {
            @Override
            public void onBillingUnavailable() {
                showMessage("onBillingUnavailable");
            }

            @Override
            public void onPurchasesLoaded(List<SkuPurchase> skuPurchases) {
                showMessage("purchases=" + skuPurchases);
            }

            @Override
            public void onPurchasesLoadFailed(BillingrException error) {
                showMessage("error=" + error);
            }
        });
    }

    private void showMessage(String onBillingUnavailable) {
        Snackbar.make(binding.getRoot(), onBillingUnavailable, Snackbar.LENGTH_LONG)
                .show();
    }
}
