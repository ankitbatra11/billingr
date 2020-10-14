package com.abatra.billingr.gson;

import android.util.Log;

import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuType;
import com.abatra.billingr.google.GoogleSku;
import com.android.billingclient.api.SkuDetails;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.lang.reflect.Type;

public class SkuDeserializer implements JsonDeserializer<Sku> {

    private static final String LOG_TAG = "SkuDeserializer";

    @Override
    public Sku deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Sku sku = null;
        JsonObject skuJsonObject = json.getAsJsonObject();
        String affiliation = skuJsonObject.get(SkuSerde.PROP_AFFILIATION).getAsString();
        if (Sku.AFFILIATION_GOOGLE.equals(affiliation)) {
            SkuType skuType = SkuType.fromValue(skuJsonObject.get(SkuSerde.PROP_TYPE).getAsInt());
            SkuDetails skuDetails = createSkuDetails(skuJsonObject.get(SkuSerde.PROP_JSON).getAsString());
            sku = new GoogleSku(skuType, skuDetails);
        }
        return sku;
    }

    private SkuDetails createSkuDetails(String json) {
        try {
            return new SkuDetails(json);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to creating SkuDetails!", e);
            throw new RuntimeException(e);
        }
    }
}
