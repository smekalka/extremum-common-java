package io.extremum.dynamic.schema.provider.networknt.impl;

import io.extremum.dynamic.resources.LocalResourceLoader;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.AbstractNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.NetworkntURIFetcher;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Slf4j
public class FileSystemNetworkntSchemaProvider extends AbstractNetworkntSchemaProvider {
    private final Path schemaDirectory;
    private final FileNetworkntURIFetcher fetcher;
    private final ResourceLoader resourceLoader;

    public FileSystemNetworkntSchemaProvider(JsonSchemaType type, Path schemaDirectory) {
        super(type);
        this.schemaDirectory = schemaDirectory;
        this.resourceLoader = new LocalResourceLoader();
        this.fetcher = new FileNetworkntURIFetcher(schemaDirectory.toString(), resourceLoader);
    }

    @Override
    protected ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    protected List<NetworkntURIFetcher> getUriFetchers() {
        return Collections.singletonList(fetcher);
    }

    @Override
    protected URI makeSchemaUri(String schemaName) {
        Path path = Paths.get(schemaDirectory.toString(), schemaName);
        return URI.create("file:/").resolve(path.toString());
    }
}
