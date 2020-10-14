package com.abatra.billingr.sku;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class QuerySkuRequest {

    private final Map<SkuType, Collection<String>> skuIdsByType = new HashMap<>();
    private SkuListener skuListener;
    private boolean queryFromCache;

    private QuerySkuRequest() {
    }

    public Map<SkuType, Collection<String>> getSkuIdsByType() {
        return skuIdsByType;
    }

    public SkuListener getSkuListener() {
        return skuListener;
    }

    public boolean queryFromCache() {
        return queryFromCache;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<SkuType, Collection<String>> skuIdsByType = new HashMap<>();
        private SkuListener skuListener;
        private boolean queryFromCache;

        private Builder() {
        }

        public Builder forSku(Map<SkuType, Collection<String>> skuIdsByType) {
            this.skuIdsByType.clear();
            this.skuIdsByType.putAll(skuIdsByType);
            return this;
        }

        public Builder setQueryFromCache(boolean queryFromCache) {
            this.queryFromCache = queryFromCache;
            return this;
        }

        public Builder forSku(SkuType skuType, String... skuId) {
            Collection<String> skuIds = skuIdsByType.get(skuType);
            if (skuIds == null) {
                skuIds = new HashSet<>();
                skuIdsByType.put(skuType, skuIds);
            }
            skuIds.addAll(Arrays.asList(skuId));
            return this;
        }

        public Builder setSkuListener(SkuListener skuListener) {
            this.skuListener = skuListener;
            return this;
        }

        public QuerySkuRequest build() {
            QuerySkuRequest querySkuRequest = new QuerySkuRequest();
            querySkuRequest.skuIdsByType.putAll(skuIdsByType);
            querySkuRequest.skuListener = skuListener;
            querySkuRequest.queryFromCache = queryFromCache;
            return querySkuRequest;
        }
    }
}
