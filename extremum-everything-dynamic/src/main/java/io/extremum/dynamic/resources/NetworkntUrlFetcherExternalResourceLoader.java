package io.extremum.dynamic.resources;

import com.networknt.schema.uri.URLFetcher;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
public class NetworkntUrlFetcherExternalResourceLoader implements ExternalResourceLoader {
    private final URLFetcher urlFetcher = new URLFetcher();

    @Override
    public InputStream loadAsInputStream(URI uri) throws ResourceLoadingException {
        try {
            return urlFetcher.fetch(uri);
        } catch (FileNotFoundException e) {
            log.error("Resource {} is not found", uri, e);
            throw new ResourceNotFoundException(uri, e);
        } catch (IOException e) {
            log.error("Unable to load resource {}", uri, e);
            throw new ResourceLoadingException(uri, e);
        }
    }
}
