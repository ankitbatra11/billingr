package com.abatra.billingr.utils;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class WeakReferenceUtils {

    private WeakReferenceUtils() {
    }

    @Nullable
    public static <T> WeakReference<T> createWeakReference(@Nullable T object) {
        return object != null ? new WeakReference<>(object) : null;
    }
}
