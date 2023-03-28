package io.extremum.dynamic.models.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
@Getter
@ModelName(DynamicModel.MODEL_TYPE)
@AllArgsConstructor
public class JsonDynamicModel implements DynamicModel<Map<String, Object>>, BasicModel<Descriptor> {
    // the schema version to set, if schema version was not specified at constructor
    private final static int DEFAULT_SCHEMA_VERSION = 1;

    public JsonDynamicModel(Descriptor id, String modelName, Map<String, Object> modelData) {
        this.id = id;
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = modelName;
        this.schemaVersion = DEFAULT_SCHEMA_VERSION;
    }

    public JsonDynamicModel(String modelName, Map<String, Object> modelData) {
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = modelName;
        this.schemaVersion = DEFAULT_SCHEMA_VERSION;
    }

    public JsonDynamicModel(String modelName, Map<String, Object> modelData, String schemaName, Integer schemaVersion) {
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
    }

    private Descriptor id;
    private String iri;
    private final String modelName;
    private final Map<String, Object> modelData;
    private final String schemaName;
    private final int schemaVersion;

    @JsonIgnore
    @Override
    public Descriptor getUuid() {
        return id;
    }

    @JsonIgnore
    @Override
    public void setUuid(Descriptor uuid) {
        throw new UnsupportedOperationException();
    }
}
