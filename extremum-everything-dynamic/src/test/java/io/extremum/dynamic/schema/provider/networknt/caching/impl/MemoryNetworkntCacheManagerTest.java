package io.extremum.dynamic.schema.provider.networknt.caching.impl;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MemoryNetworkntCacheManagerTest {
    @Test
    void cacheContainsASchemaAfterCaching() {
        MemoryNetworkntCacheManager manager = new MemoryNetworkntCacheManager();

        NetworkntSchema schema = mock(NetworkntSchema.class);

        String pointer = "main.schema.json";

        manager.cacheSchema(schema, pointer);

        Optional<NetworkntSchema> loaded = manager.fetchFromCache(pointer);

        assertTrue(loaded.isPresent());
        assertEquals(schema, loaded.get());
        assertEquals(schema.getSchema(), loaded.get().getSchema());
    }

    @Test
    void cacheIsNotContainsASchema() {
        MemoryNetworkntCacheManager manager = new MemoryNetworkntCacheManager();

        Optional<NetworkntSchema> loaded = manager.fetchFromCache("unknownSchema");

        assertNotNull(loaded);
        assertFalse(loaded.isPresent());
    }

    @Test
    void removeSchemaTest() {
        MemoryNetworkntCacheManager manager = new MemoryNetworkntCacheManager();

        String pointer = "pointer";
        manager.cacheSchema(mock(NetworkntSchema.class), pointer);

        assertTrue(manager.fetchFromCache(pointer).isPresent());

        manager.removeFromCache(pointer);

        assertFalse(manager.fetchFromCache(pointer).isPresent());
    }
}