package com.abatra.billingr.gson;

import com.abatra.billingr.sku.Sku;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import static com.abatra.billingr.gson.SkuSerde.PROP_AFFILIATION;
import static com.abatra.billingr.gson.SkuSerde.PROP_JSON;
import static com.abatra.billingr.gson.SkuSerde.PROP_TYPE;

public class SkuSerializer implements JsonSerializer<Sku> {

    @Override
    public JsonElement serialize(Sku src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject skuJsonObject = new JsonObject();
        skuJsonObject.addProperty(PROP_AFFILIATION, src.getAffiliation());
        skuJsonObject.addProperty(PROP_TYPE, src.getType().getValue());
        skuJsonObject.addProperty(PROP_JSON, src.getJson());
        return skuJsonObject;
    }
}
