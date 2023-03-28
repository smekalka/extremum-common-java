package io.extremum.dynamic.schema.networknt;

import com.networknt.schema.JsonSchema;
import io.extremum.dynamic.schema.Schema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NetworkntSchema implements Schema<JsonSchema> {
    private final JsonSchema schema;

    @Override
    public JsonSchema getSchema() {
        return schema;
    }
}
