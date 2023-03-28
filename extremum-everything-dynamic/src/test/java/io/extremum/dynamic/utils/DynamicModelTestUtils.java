package io.extremum.dynamic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.SneakyThrows;

import java.util.Map;

public class DynamicModelTestUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static Map<String, Object> toMap(String value) {
        return mapper.readerFor(Map.class).readValue(value);
    }

    public static JsonDynamicModel buildModel(String modelName, Map<String, Object> data) {
        return buildModel(null, modelName, data);
    }

    public static JsonDynamicModel buildModel(Descriptor id, String modelName, Map<String, Object> data) {
        return new JsonDynamicModel(id, modelName, data);
    }
}
