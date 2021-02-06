package com.abatra.billingr;

public enum SkuType {
    IN_APP_PRODUCT(0),
    SUBSCRIPTION(1);

    private final int value;

    SkuType(int value) {
        this.value = value;
    }

    public static SkuType fromValue(int value) {
        for (SkuType skuType : SkuType.values()) {
            if (value == skuType.getValue()) {
                return skuType;
            }
        }
        throw new IllegalArgumentException("Unknown value=" + value);
    }

    public int getValue() {
        return value;
    }
}
