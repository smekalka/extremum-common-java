package integration.everythingmanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import configurations.FileSystemSchemaProviderConfiguration;
import integration.SpringBootTestWithServices;
import io.atlassian.fugue.Try;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.everything.management.HybridEverythingManagementService;
import io.extremum.dynamic.everything.management.ReactiveDynamicModelEverythingManagementService;
import io.extremum.dynamic.metadata.impl.DefaultDynamicModelMetadataProviderService;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.starter.CommonConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.processor.ReactiveWatchEventConsumer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static io.extremum.dynamic.utils.DynamicModelTestUtils.toMap;
import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(classes = {
        CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        FileSystemSchemaProviderConfiguration.class,
        DynamicModuleAutoConfiguration.class
})
@MockBeans({
        @MockBean(RoleSecurity.class),
        @MockBean(DataSecurity.class),
        @MockBean(PrincipalSource.class),
        @MockBean(ModelSaver.class)
})
public class ReactiveDynamicModelEverythingManagementServiceTest extends SpringBootTestWithServices {
    private static final Descriptor NOT_EXISTENT_DESCRIPTOR = Descriptor.builder()
            .internalId("000000000000000000000000")
            .externalId("00000000-0000-0000-0000-000000000000")
            .build();

    @Autowired
    HybridEverythingManagementService hybridEverythingManagementService;

    @Autowired
    JsonBasedDynamicModelService dynamicModelService;

    @Autowired
    JsonDynamicModelDao dynamicModelDao;

    @Autowired
    ReactiveDescriptorDeterminator reactiveDescriptorDeterminator;

    @MockBean
    SchemaMetaService schemaMetaService;

    @MockBean
    JsonDynamicModelValidator jsonDynamicModelValidator;

    @MockBean
    DefaultDynamicModelMetadataProviderService metadataProvider;

    @MockBean
    ReactiveWatchEventConsumer watchEventConsumer;

    ReactiveDynamicModelEverythingManagementService dynamicModelEverythingManagementService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void beforeEach() {
        doReturn(just(Try.successful(mock(ValidationContext.class))))
                .when(jsonDynamicModelValidator).validate(any());

        dynamicModelEverythingManagementService =
                hybridEverythingManagementService.getDynamicModelEverythingManagementService();
    }

    @Test
    void getOperation_shouldReturn_model() throws IOException {
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        JsonDynamicModel model = createModel("TestDynamicModel", "{\"a\":\"b\"}");
        JsonDynamicModel savedModel = dynamicModelService.saveModel(model).block();

        ResponseDto found = dynamicModelEverythingManagementService.get(savedModel.getId(), false).block();

        assertNotNull(found);
        assertEquals(savedModel.getId(), found.getId());
        assertEquals(model.getModelName(), found.getModel());
        assertTrue(((JsonDynamicModelResponseDto) found).getData().containsKey("a"));
        assertEquals("b", ((JsonDynamicModelResponseDto) found).getData().get("a"));

        String serialized = serialize(found);
        JsonNode deserialized = deserializeToNode(serialized);

        checkServiceFields(deserialized);
    }

    private void checkServiceFields(JsonNode deserialized) {
        assertTrue(deserialized.has(created.getStringValue()));
        assertTrue(deserialized.has(model.getStringValue()));
        assertTrue(deserialized.has(version.getStringValue()));
    }

    private JsonNode deserializeToNode(String serialized) throws IOException {
        return mapper.readValue(serialized, JsonNode.class);
    }

    private String serialize(ResponseDto found) throws JsonProcessingException {
        return mapper.writeValueAsString(found);
    }

    @Test
    void getOperation_shouldReturn_NotFoundException_if_modelIsNotExists() {
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.get(NOT_EXISTENT_DESCRIPTOR, false);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void patchOperation_shouldPerformPatching_andReturnAPatchedModel() throws IOException, JSONException {
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        String modelName = "PatchingDynamicModel";
        int schemaVersion = 1;

        doReturn(modelName).when(schemaMetaService).getSchemaName(modelName, schemaVersion);

        JsonDynamicModel patchingModel = createModel(modelName, "{\"a\":\"b\"}");
        JsonDynamicModel saved = dynamicModelDao.create(patchingModel, patchingModel.getModelName().toLowerCase()).block();

        JsonNode nodePatch = createJsonNodeForString("[{\"op\":\"replace\", \"path\":\"/a\", \"value\":\"c\"}]");
        JsonPatch patch = JsonPatch.fromJson(nodePatch);
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.patch(saved.getId(), patch, false);

        StepVerifier.create(result)
                .assertNext(patched -> {
                    assertEquals(saved.getId(), patched.getId());
                    assertEquals(patchingModel.getModelName(), patched.getModel());
                    assertEquals("c", ((JsonDynamicModelResponseDto) patched).getData().get("a"));
                }).verifyComplete();

        Mono<JsonDynamicModel> foundPatchedModel = dynamicModelService.findById(saved.getId());
        StepVerifier.create(foundPatchedModel)
                .assertNext(patched -> {
                    assertEquals(saved.getId(), patched.getId());
                    assertEquals(patchingModel.getModelName(), patched.getModelName());
                    assertEquals("c", patched.getModelData().get("a"));
                }).verifyComplete();

        ArgumentCaptor<TextWatchEvent> eventCaptor = ArgumentCaptor.forClass(TextWatchEvent.class);
        verify(watchEventConsumer, times(1)).consume(eventCaptor.capture());

        JSONArray jArray = new JSONArray(eventCaptor.getValue().getJsonPatch());
        assertEquals(1, jArray.length());
        assertEquals("replace", jArray.getJSONObject(0).getString("op"));
        assertEquals("/a", jArray.getJSONObject(0).getString("path"));
        assertEquals("c", jArray.getJSONObject(0).getString("value"));
    }

    @Test
    void patchOperation_shouldReturnsWithNotFoundException_if_modelIsNotExists() throws IOException {
        JsonNode nodePatch = createJsonNodeForString("[{\"op\":\"replace\", \"path\":\"/a\", \"value\":\"c\"}]");
        JsonPatch patch = JsonPatch.fromJson(nodePatch);
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.patch(NOT_EXISTENT_DESCRIPTOR, patch, false);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();

        verify(watchEventConsumer, never()).consume(any());
    }

    @Test
    void removeOperation_shouldRemoveModel_andReturnsWithEmptyPipe() throws JSONException {
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        JsonDynamicModel model = createModel("ModelForRemove", "{\"a\":\"b\"}");
        JsonDynamicModel savedModel = dynamicModelDao.create(model, model.getModelName().toLowerCase()).block();

        Mono<Void> result = dynamicModelEverythingManagementService.remove(savedModel.getId());

        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<TextWatchEvent> eventCaptor = ArgumentCaptor.forClass(TextWatchEvent.class);
        verify(watchEventConsumer, times(1)).consume(eventCaptor.capture());

        assertNotNull(eventCaptor.getValue());

        JSONArray jArray = new JSONArray(eventCaptor.getValue().getJsonPatch());
        assertEquals(1, jArray.length());

        JSONObject op = jArray.getJSONObject(0);
        assertEquals("remove", op.get("op"));
        assertEquals("/", op.get("path"));
    }

    @Test
    void removeOperation_shouldReturnsWithEmptyPipe_if_modelIsNotExists() {
        Mono<Void> result = dynamicModelEverythingManagementService.remove(NOT_EXISTENT_DESCRIPTOR);

        StepVerifier.create(result).verifyComplete();

        verify(watchEventConsumer, never()).consume(any());
    }

    private JsonDynamicModel createModel(String modelName, String stringModelData) {
        return new JsonDynamicModel(modelName, toMap(stringModelData));
    }

    private JsonNode createJsonNodeForString(String stringModelData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(stringModelData, JsonNode.class);
        } catch (IOException e) {
            String msg = format("Unable to create JsonNode from source %s: %s", stringModelData, e);
            fail(msg);
            throw new RuntimeException(msg, e);
        }
    }
}
