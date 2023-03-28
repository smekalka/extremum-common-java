package io.extremum.dynamic.schema.provider.networknt;

import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.SchemaProvider;

public interface NetworkntSchemaProvider extends SchemaProvider<NetworkntSchema> {
    JsonSchemaType getSchemaType();
}
