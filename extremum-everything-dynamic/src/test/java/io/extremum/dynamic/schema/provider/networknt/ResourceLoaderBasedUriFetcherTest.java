package io.extremum.dynamic.schema.provider.networknt;

import io.extremum.dynamic.resources.ResourceLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ResourceLoaderBasedUriFetcherTest {
    @Test
    void fetch() throws IOException {
        ResourceLoader loader = mock(ResourceLoader.class);

        ResourceLoaderBasedUriFetcher fetcher = new ResourceLoaderBasedUriFetcher(loader);

        URI uri = URI.create("http://localhost/path/to/resource");
        fetcher.fetch(uri);

        verify(loader).loadAsInputStream(uri);
    }
}