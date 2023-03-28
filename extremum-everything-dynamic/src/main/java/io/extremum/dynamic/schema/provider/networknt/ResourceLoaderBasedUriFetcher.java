package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.uri.URIFetcher;
import io.extremum.dynamic.resources.ResourceLoader;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

@RequiredArgsConstructor
public class ResourceLoaderBasedUriFetcher implements URIFetcher, NetworkntURIFetcher {
    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("http", "https")));

    private final ResourceLoader resourceLoader;

    @Override
    public InputStream fetch(URI uri) throws IOException {
        return resourceLoader.loadAsInputStream(uri);
    }

    @Override
    public List<String> getSupportedSchemas() {
        return new ArrayList<>(SUPPORTED_SCHEMES);
    }
}
