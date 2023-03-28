package io.extremum.dynamic;

import java.util.Set;

public interface SchemaMetaService {

    String getSchemaName(String modelName, int schemaVersion);

    void registerMapping(String modelName, String schemaName, int schemaVersion);

    Set<String> getModelNames();

    Set<String> getRegisteredSchemas();
}
