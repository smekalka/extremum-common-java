package io.extremum.dynamic.schema.provider;

import io.extremum.dynamic.schema.Schema;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;

public interface SchemaProvider<S extends Schema<?>> {
    S loadSchema(String schemaName) throws SchemaLoadingException;
}
