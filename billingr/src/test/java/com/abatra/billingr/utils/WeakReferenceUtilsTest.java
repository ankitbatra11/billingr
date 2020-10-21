package com.abatra.billingr.utils;

import org.junit.Test;

import java.lang.ref.WeakReference;

import static com.abatra.billingr.utils.WeakReferenceUtils.createWeakReference;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class WeakReferenceUtilsTest {

    @Test
    public void testCreateWeakReference_nullObject() {
        assertThat(createWeakReference(null), nullValue());
    }

    @Test
    public void testCreateWeakReference_nonNullObject() {

        Object object = new Object();

        WeakReference<Object> weakReference = createWeakReference(object);

        assertThat(weakReference.get(), sameInstance(object));
    }
}
