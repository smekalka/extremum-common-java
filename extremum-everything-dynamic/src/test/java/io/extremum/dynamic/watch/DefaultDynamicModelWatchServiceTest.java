package io.extremum.dynamic.watch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.processor.ReactiveWatchEventConsumer;
import io.extremum.watch.processor.WatchEventConsumer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultDynamicModelWatchServiceTest {
    private static final String MODEL_NAME = "modelName";
    private static final Descriptor DESCRIPTOR = Descriptor.builder()
            .internalId("internal")
            .externalId("external")
            .storageType(StandardStorageType.MONGO)
            .modelType(MODEL_NAME)
            .build();

    static ObjectMapper mapper = new ObjectMapper();

    ReactiveWatchEventConsumer watchEventConsumer;

    DynamicModelWatchService watchService;

    private Map<String, Object> modelData = new HashMap<String, Object>() {{
        put("a", "b");
    }};

    @BeforeEach
    void beforeEach() {
        watchEventConsumer = mock(ReactiveWatchEventConsumer.class);
        watchService = new DefaultDynamicModelWatchService(watchEventConsumer, mapper);
    }

    @Test
    void watchPatchOperation() throws JSONException, IOException {
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        JsonDynamicModel model = new JsonDynamicModel(DESCRIPTOR, MODEL_NAME, modelData);

        JsonNode node = mapper.readValue("[{\"op\":\"add\",\"path\":\"/tags/-\",\"value\": \"v\"}]", JsonNode.class);

        JsonPatch patch = JsonPatch.fromJson(node);
        watchService.registerPatchOperation(patch, model).block();

        ArgumentCaptor<TextWatchEvent> captor = ArgumentCaptor.forClass(TextWatchEvent.class);

        verify(watchEventConsumer).consume(captor.capture());

        TextWatchEvent captured = captor.getValue();

        String jsonPatch = captured.getJsonPatch();

        JSONArray jsonArray = new JSONArray(jsonPatch);
        JSONObject json = jsonArray.getJSONObject(0);
        String value = json.getString("value");

        assertEquals("v", value);
        assertEquals("add", json.getString("op"));
        assertEquals("/tags/-", json.getString("path"));

        assertEquals(DESCRIPTOR.getInternalId(), captured.getModelId());
    }

    @Test
    void watchSaveOperation() throws JSONException {
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        JsonDynamicModel model = new JsonDynamicModel(DESCRIPTOR, MODEL_NAME, modelData);

        watchService.registerSaveOperation(model).block();

        ArgumentCaptor<TextWatchEvent> captor = ArgumentCaptor.forClass(TextWatchEvent.class);

        verify(watchEventConsumer).consume(captor.capture());

        TextWatchEvent captured = captor.getValue();

        String jsonPatch = captured.getJsonPatch();

        JSONArray jsonArray = new JSONArray(jsonPatch);
        JSONObject json = jsonArray.getJSONObject(0);
        JSONObject valueObject = json.getJSONObject("value");

        assertNotNull(valueObject);
        assertEquals("replace", json.getString("op"));
        assertEquals("/", json.getString("path"));

        assertEquals(DESCRIPTOR.getInternalId(), captured.getModelId());
    }

    @Test
    void watchDeleteOperation() throws JSONException {
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        JsonDynamicModel model = new JsonDynamicModel(DESCRIPTOR, MODEL_NAME, modelData);

        watchService.registerDeleteOperation(model).block();

        ArgumentCaptor<TextWatchEvent> captor = ArgumentCaptor.forClass(TextWatchEvent.class);

        verify(watchEventConsumer).consume(captor.capture());

        TextWatchEvent captured = captor.getValue();

        String jsonPatch = captured.getJsonPatch();

        JSONArray jsonArray = new JSONArray(jsonPatch);
        JSONObject json = jsonArray.getJSONObject(0);

        assertEquals("remove", json.getString("op"));
        assertEquals("/", json.getString("path"));

        assertEquals(DESCRIPTOR.getInternalId(), captured.getModelId());
    }
}
