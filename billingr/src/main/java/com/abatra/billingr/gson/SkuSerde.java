package com.abatra.billingr.gson;

import android.util.Log;

import com.abatra.billingr.google.GoogleSku;
import com.abatra.billingr.sku.Sku;
import com.abatra.billingr.sku.SkuType;
import com.android.billingclient.api.SkuDetails;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONException;

import java.lang.reflect.Type;

public class SkuSerde implements JsonDeserializer<Sku>, JsonSerializer<Sku> {

    private static final String LOG_TAG = "SkuSerde";

    private static final String PROP_TYPE = "type";
    private static final String PROP_JSON = "json";
    private static final String PROP_AFFILIATION = "affiliation";

    @Override
    public Sku deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject skuJsonObject = json.getAsJsonObject();
        String affiliation = skuJsonObject.get(SkuSerde.PROP_AFFILIATION).getAsString();
        if (Sku.AFFILIATION_GOOGLE.equals(affiliation)) {
            SkuType skuType = SkuType.fromValue(skuJsonObject.get(SkuSerde.PROP_TYPE).getAsInt());
            SkuDetails skuDetails = createSkuDetails(skuJsonObject.get(SkuSerde.PROP_JSON).getAsString());
            return new GoogleSku(skuType, skuDetails);
        }
        throw new IllegalArgumentException("Unknown affiliation=" + affiliation);
    }

    private SkuDetails createSkuDetails(String json) {
        try {
            return new SkuDetails(json);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to create SkuDetails!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonElement serialize(Sku sku, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject skuJsonObject = new JsonObject();
        skuJsonObject.addProperty(PROP_AFFILIATION, sku.getAffiliation());
        skuJsonObject.addProperty(PROP_TYPE, sku.getType().getValue());
        skuJsonObject.addProperty(PROP_JSON, sku.getJson());
        return skuJsonObject;
    }
}
