package io.extremum.dynamic.schema.provider.networknt.impl;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
public class URIBasedNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final URI baseUri;

    private volatile JsonSchemaFactory factory;

    @Override
    public JsonSchemaType getSchemaType() {
        return JsonSchemaType.V2019_09;
    }

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        URI uri = baseUri.resolve(schemaName);

        log.info("trying to load schema {} from uri {}", schemaName, uri);

        JsonSchema schema = buildFactory().getSchema(uri);
        return new NetworkntSchema(schema);
    }

    private JsonSchemaFactory buildFactory() {
        if (factory == null) {
            synchronized (URIBasedNetworkntSchemaProvider.class) {
                if (factory == null) {
                    JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();

                    factory = new JsonSchemaFactory.Builder()
                            .addMetaSchema(metaSchema)
                            .defaultMetaSchemaURI(metaSchema.getUri())
                            .build();
                }
            }
        }

        return factory;
    }
}
