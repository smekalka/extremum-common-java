package io.extremum.dynamic.models.impl;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDynamicModelTest {
    @Test
    void JsonBasedDynamicModel() {
        String modelName = UUID.randomUUID().toString();
        Map<String, Object> node = new HashMap<>();
        node.put("a", "b");

        JsonDynamicModel model = new JsonDynamicModel(modelName, node);

        assertEquals(modelName, model.getModelName());
        assertTrue(model.getModelData().containsKey("a"));
        assertEquals(node.get("a"), model.getModelData().get("a"));
    }
}