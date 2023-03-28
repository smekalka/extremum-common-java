package io.extremum.dynamic.schema.provider.networknt.caching;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CachingNetworkntSchemaProviderTest {
    @Test
    void schemaLoadsFromCacheFirst() {
        NetworkntCacheManager cacheManager = mock(NetworkntCacheManager.class);

        CachingNetworkntSchemaProvider spyProvider = mock(
                CachingNetworkntSchemaProvider.class,
                Mockito.withSettings()
                        .useConstructor(cacheManager)
                        .defaultAnswer(CALLS_REAL_METHODS)
        );

        String schemaName = "schemaName";

        when(cacheManager.fetchFromCache(schemaName)).thenReturn(Optional.of(Mockito.mock(NetworkntSchema.class)));

        spyProvider.loadSchema(schemaName);

        verify(cacheManager, times(1)).fetchFromCache(schemaName);
        verify(spyProvider, never()).fetchSchemaForcibly(any());
    }

    @Test
    void schemaLoadsFromSource_ifCacheDoesntContainASchema_and_schemaCached() {
        NetworkntCacheManager cacheManager = mock(NetworkntCacheManager.class);

        CachingNetworkntSchemaProvider spyProvider = mock(
                CachingNetworkntSchemaProvider.class,
                Mockito.withSettings()
                        .useConstructor(cacheManager)
                        .defaultAnswer(CALLS_REAL_METHODS)
        );

        String schemaName = "schemaName";

        NetworkntSchema mockedSchema = mock(NetworkntSchema.class);

        when(cacheManager.fetchFromCache(schemaName)).thenReturn(Optional.empty());
        when(spyProvider.fetchSchemaForcibly(schemaName)).thenReturn(Optional.of(mockedSchema));

        spyProvider.loadSchema(schemaName);

        verify(spyProvider, times(1)).fetchSchemaForcibly(schemaName);
        verify(cacheManager, times(1)).cacheSchema(mockedSchema, schemaName);
    }

    @Test
    void exceptionThrown_when_schemaIsNotFound() {
        NetworkntCacheManager cacheManager = mock(NetworkntCacheManager.class);

        CachingNetworkntSchemaProvider spyProvider = mock(
                CachingNetworkntSchemaProvider.class,
                Mockito.withSettings()
                        .useConstructor(cacheManager)
                        .defaultAnswer(CALLS_REAL_METHODS)
        );

        String schemaName = "schemaName";

        when(cacheManager.fetchFromCache(schemaName)).thenReturn(Optional.empty());
        when(spyProvider.fetchSchemaForcibly(schemaName)).thenReturn(Optional.empty());

        assertThrows(SchemaNotFoundException.class, () -> spyProvider.loadSchema(schemaName));

        verify(cacheManager, times(1)).fetchFromCache(schemaName);
        verify(spyProvider, times(1)).fetchSchemaForcibly(schemaName);
        verify(cacheManager, never()).cacheSchema(any(), any());
    }
}
