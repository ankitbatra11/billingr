package com.abatra.billingr;

abstract public class AbstractBillingrBuilder implements BillingrBuilder {

    private boolean analyticsEnabled = false;

    @Override
    public void withAnalyticsEnabled(boolean analyticsEnabled) {
        this.analyticsEnabled = analyticsEnabled;
    }

    @Override
    public Billingr build() {
        return build(analyticsEnabled);
    }

    protected abstract Billingr build(boolean analyticsEnabled);
}
