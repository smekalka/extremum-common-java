package io.extremum.dynamic.schema.provider.networknt.impl;

import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import io.extremum.dynamic.schema.provider.networknt.NetworkntURIFetcher;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FileNetworkntURIFetcher implements NetworkntURIFetcher {
    private final String basicDirectory;
    private final ResourceLoader resourceLoader;

    @Override
    public List<String> getSupportedSchemas() {
        return Collections.singletonList("file");
    }

    @Override
    public InputStream fetch(URI uri) throws IOException {
        String fileName = uri.toString().substring(uri.getScheme().length() + 1);
        URI path = URI.create("file:/")
                .resolve(Paths.get(basicDirectory, fileName).toString());
        try {
            return resourceLoader.loadAsInputStream(path);
        } catch (ResourceLoadingException e) {
            log.error("Unable to load schema {} from directory {}", fileName, basicDirectory);

            if (e instanceof ResourceNotFoundException) {
                throw new SchemaNotFoundException(fileName, e);
            } else {
                throw new SchemaLoadingException(fileName, e);
            }
        }
    }
}
