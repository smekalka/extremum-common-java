package io.extremum.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.extremum.sharedmodels.basic.ModelSettings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ModelSchema implements Serializable {
    private String name;
    private JsonNode schema;
    private ModelSettings settings;

    public ModelSchema(String name, JsonNode schema, ModelSettings settings) {
        this.name = name;
        this.schema = schema;
        this.settings = settings;
    }
}
