package com.abatra.billingr;

abstract public class AbstractBillingrBuilder implements BillingrBuilder {

    protected boolean analyticsEnabled = false;

    @Override
    public void setAnalyticsEnabled(boolean analyticsEnabled) {
        this.analyticsEnabled = analyticsEnabled;
    }
}
