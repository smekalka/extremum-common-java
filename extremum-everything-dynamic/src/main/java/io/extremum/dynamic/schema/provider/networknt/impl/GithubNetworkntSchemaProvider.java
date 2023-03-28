package io.extremum.dynamic.schema.provider.networknt.impl;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.uri.URIFetcher;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.github.GithubAccessOptions;
import io.extremum.dynamic.resources.github.GithubContentsResourceLoader;
import io.extremum.dynamic.resources.github.GithubResourceConfiguration;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.QueryParamsPreservingURIFactory;
import io.extremum.dynamic.schema.provider.networknt.ResourceLoaderBasedUriFetcher;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class GithubNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final JsonSchemaType type;
    private final GithubResourceConfiguration githubResourceConfiguration;
    private final GithubAccessOptions githubAccessOptions;

    @Override
    public JsonSchemaType getSchemaType() {
        return type;
    }

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();

        GithubContentsResourceLoader loader = new GithubContentsResourceLoader(githubAccessOptions);

        URIFetcher uriFetcher = new ResourceLoaderBasedUriFetcher(loader);
        URIFactory uriFactory = new QueryParamsPreservingURIFactory();

        JsonSchemaFactory factory = new JsonSchemaFactory.Builder()
                .addMetaSchema(metaSchema)
                .defaultMetaSchemaURI(metaSchema.getUri())
                .uriFetcher(uriFetcher, ResourceLoaderBasedUriFetcher.SUPPORTED_SCHEMES.toArray(new String[] {}))
                .uriFactory(uriFactory, QueryParamsPreservingURIFactory.SUPPORTED_SCHEMES.toArray(new String[] {}))
                .build();

        URI uri = makeUrl(schemaName, githubResourceConfiguration);
        try {
            JsonSchema schema = factory.getSchema(uri);
            return new NetworkntSchema(schema);
        } catch (ResourceLoadingException e) {
            String msg = format("Unable to load schema from %s", uri);
            log.error(msg, e);
            throw new SchemaLoadingException(msg, e);
        }
    }

    private URI makeUrl(String schemaName, GithubResourceConfiguration conf) {
        Path path = Paths.get("/repos",
                conf.getOwner(),
                conf.getRepo(),
                "/contents",
                conf.getSchemaPath(),
                format("%s?ref=%s", schemaName, conf.getRef()));

        return URI.create(conf.getGithubApiBase()).resolve(path.toString());
    }
}
