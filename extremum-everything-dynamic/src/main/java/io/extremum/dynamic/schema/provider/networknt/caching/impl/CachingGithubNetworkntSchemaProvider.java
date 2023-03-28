package io.extremum.dynamic.schema.provider.networknt.caching.impl;

import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.caching.CachingNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class CachingGithubNetworkntSchemaProvider extends CachingNetworkntSchemaProvider {
    private final GithubNetworkntSchemaProvider githuSchemaProvider;

    public CachingGithubNetworkntSchemaProvider(NetworkntCacheManager cacheManager, GithubNetworkntSchemaProvider githuSchemaProvider) {
        super(cacheManager);
        this.githuSchemaProvider = githuSchemaProvider;
    }

    @Override
    protected Optional<NetworkntSchema> fetchSchemaForcibly(String schemaPointer) {
        try {
            return Optional.of(githuSchemaProvider.loadSchema(schemaPointer));
        } catch (SchemaLoadingException e) {
            log.error("Unable to load schema {}", schemaPointer, e);
            return Optional.empty();
        }
    }

    @Override
    public JsonSchemaType getSchemaType() {
        return githuSchemaProvider.getSchemaType();
    }
}
