package io.extremum.dynamic.schema.provider.networknt.caching;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;

import java.util.Optional;

public interface NetworkntCacheManager {
    void cacheSchema(NetworkntSchema schema, String pointer);

    Optional<NetworkntSchema> fetchFromCache(String pointer);

    void removeFromCache(String pointer);

    void clearCache();
}
