package io.extremum.dynamic.schema.provider.networknt.caching;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class CachingNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final NetworkntCacheManager cacheManager;

    @Override
    public NetworkntSchema loadSchema(String schemaPointer) throws SchemaLoadingException {
        Optional<NetworkntSchema> loaded = cacheManager.fetchFromCache(schemaPointer);

        return loaded.orElseGet(fetchSchemaFromSource(schemaPointer));
    }

    private Supplier<NetworkntSchema> fetchSchemaFromSource(String schemaPointer) {
        return () -> fetchSchemaForcibly(schemaPointer)
                .map(fetchedSchema -> {
                    cacheManager.cacheSchema(fetchedSchema, schemaPointer);
                    return fetchedSchema;
                })
                .orElseThrow(() -> new SchemaNotFoundException(schemaPointer));
    }

    abstract protected Optional<NetworkntSchema> fetchSchemaForcibly(String schemaPointer);
}
