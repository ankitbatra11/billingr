package com.abatra.billingr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class QuerySkuRequest {

    private final Map<SkuType, Collection<String>> skuIdsByType = new HashMap<>();
    private SkuListener skuListener;

    private QuerySkuRequest() {
    }

    public Map<SkuType, Collection<String>> getSkuIdsByType() {
        return skuIdsByType;
    }

    public SkuListener getSkuListener() {
        return skuListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<SkuType, Collection<String>> skuIdsByType = new HashMap<>();
        private SkuListener skuListener;

        private Builder() {
        }

        public Builder forSku(SkuType skuType, String skuId) {
            Collection<String> skuIds = skuIdsByType.get(skuType);
            if (skuIds == null) {
                skuIds = new HashSet<>();
                skuIdsByType.put(skuType, skuIds);
            }
            skuIds.add(skuId);
            return this;
        }

        public QuerySkuRequest build() {
            QuerySkuRequest querySkuRequest = new QuerySkuRequest();
            querySkuRequest.skuIdsByType.putAll(skuIdsByType);
            querySkuRequest.skuListener = skuListener;
            return querySkuRequest;
        }
    }
}
