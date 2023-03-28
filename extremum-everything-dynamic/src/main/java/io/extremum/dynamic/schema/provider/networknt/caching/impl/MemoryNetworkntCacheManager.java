package io.extremum.dynamic.schema.provider.networknt.caching.impl;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryNetworkntCacheManager implements NetworkntCacheManager {
    private final Map<String, NetworkntSchema> cache = new ConcurrentHashMap<>();

    @Override
    public void cacheSchema(NetworkntSchema schema, String pointer) {
        cache.put(pointer, schema);
    }

    @Override
    public Optional<NetworkntSchema> fetchFromCache(String pointer) {
        NetworkntSchema schema = cache.get(pointer);
        return Optional.ofNullable(schema);
    }

    @Override
    public void removeFromCache(String pointer) {
        cache.remove(pointer);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
