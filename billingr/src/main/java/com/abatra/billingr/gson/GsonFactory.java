package com.abatra.billingr.gson;

import com.abatra.billingr.google.GoogleSku;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {

    private GsonFactory() {
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(GoogleSku.class, new SkuSerde())
                .create();
    }
}

