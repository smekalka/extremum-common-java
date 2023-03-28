package io.extremum.dynamic;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultSchemaMetaService implements SchemaMetaService {

    private final Map<SchemaKey, String> map = new ConcurrentHashMap<>();

    @Override
    public String getSchemaName(String modelName, int schemaVersion) {
        return map.get(new SchemaKey(modelName, schemaVersion));
    }

    @Override
    public void registerMapping(String modelName, String schemaName, int schemaVersion) {
        map.put(new SchemaKey(modelName, schemaVersion), schemaName);
    }

    @Override
    public Set<String> getModelNames() {
        return ImmutableSet.copyOf(map.keySet().stream().map(SchemaKey::getSchemaName).collect(Collectors.toSet()));
    }

    @Override
    public Set<String> getRegisteredSchemas() {
        return ImmutableSet.copyOf(map.keySet().stream().map(SchemaKey::toString).collect(Collectors.toSet()));
    }

    @Data
    @AllArgsConstructor
    private static class SchemaKey {
        String schemaName;
        int schemaVersion;

        @Override
        public String toString() {
            return schemaName.toLowerCase() + ".v" + schemaVersion;
        }
    }
}