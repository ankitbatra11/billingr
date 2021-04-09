package com.abatra.billingr;

import android.content.Context;

import com.abatra.billingr.google.GoogleBillingrBuilder;

public interface BillingrBuilder {

    static BillingrBuilder google(Context context) {
        return new GoogleBillingrBuilder(context.getApplicationContext());
    }

    void setAnalyticsEnabled(boolean analyticsEnabled);

    Billingr build();
}
