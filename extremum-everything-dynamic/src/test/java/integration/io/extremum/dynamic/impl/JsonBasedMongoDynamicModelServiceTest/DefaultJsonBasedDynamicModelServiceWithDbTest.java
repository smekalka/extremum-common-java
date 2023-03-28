package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.networknt.schema.JsonSchema;
import configurations.FileSystemSchemaProviderConfiguration;
import integration.SpringBootTestWithServices;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.metadata.MetadataProviderService;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.services.impl.DefaultJsonBasedDynamicModelService;
import io.extremum.security.*;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.starter.CommonConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.processor.ReactiveWatchEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static io.extremum.common.model.VersionedModel.FIELDS.*;
import static io.extremum.datetime.DateUtils.*;
import static io.extremum.dynamic.utils.DynamicModelTestUtils.*;
import static io.extremum.sharedmodels.basic.Model.FIELDS.deleted;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.*;

@Slf4j
@ContextConfiguration(classes = {
        CommonConfiguration.class,
        FileSystemSchemaProviderConfiguration.class,
        DynamicModuleAutoConfiguration.class
})
@MockBeans({
        @MockBean(ReactiveRoleSecurity.class),
        @MockBean(ReactiveDataSecurity.class),
        @MockBean(ReactivePrincipalSource.class)
})
class DefaultJsonBasedDynamicModelServiceWithDbTest extends SpringBootTestWithServices {
    @Autowired
    DefaultJsonBasedDynamicModelService service;

    @Autowired
    ReactiveMongoOperations operations;

    @SpyBean
    NetworkntCacheManager networkntCacheManager;

    @MockBean
    MetadataProviderService metadataProvider;

    @Autowired
    SchemaMetaService schemaMetaService;

    @MockBean
    ReactiveWatchEventConsumer watchConsumer;

    private Mono<JsonDynamicModel> saveModel(JsonDynamicModel model) {
        when(watchConsumer.consume(any())).thenReturn(Mono.empty());
        return service.saveModel(model);
    }

    @Test
    void validModelSavedInMongo() throws JSONException {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        Map<String, Object> modelData = toMap("{\n" +
                "  \"field1\": \"aaa\",\n" +
                "  \"field3\": {\n" +
                "    \"externalField\": \"bbb\"\n" +
                "  " +
                "},\n" +
                "  \"fieldObject\": {\n" +
                "    \"fieldDate1\": \"2013-01-09T09:31:26.111111-05:00\",\n" +
                "    \"fieldDate2\": \"2014-01-09T09:31:26.111111-05:00\"\n" +
                "  }\n" +
                "}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        schemaMetaService.registerMapping(model.getModelName(), "complex.schema.json", 1);

        Mono<JsonDynamicModel> saved = saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(m -> {
                            assertEquals(model.getModelName(), m.getModelName());

                            assertFalse(model.getModelData().containsKey("_id"));

                            assertEquals(model.getModelData().get("field1").toString(), m.getModelData().get("field1").toString());

                            Map<String, Object> map = ((Map) m.getModelData().get("field3"));

                            assertTrue(map.containsKey("externalField"));
                            assertEquals(((Map) model.getModelData().get("field3")).get("externalField"), map.get("externalField"));

                            assertNotNull(m.getId());
                            assertEquals(model.getModelName(), m.getId().getModelType());
                        }
                )
                .verifyComplete();

        ArgumentCaptor<TextWatchEvent> eventCaptor = ArgumentCaptor.forClass(TextWatchEvent.class);
        verify(watchConsumer, times(1)).consume(eventCaptor.capture());

        JSONArray jsonArray = new JSONArray(eventCaptor.getValue().getJsonPatch());
        assertEquals(1, jsonArray.length());
        JSONObject json = jsonArray.getJSONObject(0);
        assertEquals("replace", json.getString("op"));
        assertEquals("/", json.getString("path"));

        for (String dateField : Arrays.asList("fieldObject.fieldDate1", "fieldObject.fieldDate2")) {
            Iterable<Document> documents = operations.getCollection("complex_schema_json")
                    .flatMapMany(collection -> {
                        FindPublisher<Document> publisher = collection
                                .find(new Document(dateField, new Document("$type", "date")))
                                .limit(2);
                        return Flux.from(publisher);
                    })
                    .toIterable();

            assertEquals(1, StreamSupport.stream(documents.spliterator(), false).count());
        }
    }

    @Test
    void getModelById() {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        Map<String, Object> modelData = toMap("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        schemaMetaService.registerMapping(model.getModelName(), "empty.schema.json", 1);

        JsonDynamicModel saved = saveModel(model).block();

        JsonDynamicModel found = service.findById(saved.getId()).block();

        assertNotNull(found);
        assertNotNull(found.getId());
        assertNotNull(model.getModelName(), found.getId().getModelType());
        assertEquals(saved.getId(), found.getId());
        assertEquals(model.getModelName(), found.getModelName());
        assertFalse(model.getModelData().containsKey("_id"));
        assertEquals(model.getModelData().get("field1"), found.getModelData().get("field1"));
        assertEquals(model.getModelData().get("field3"), found.getModelData().get("field3"));
    }

    @Test
    void updateModelTest() {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        String modelName = "complexModel";

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), modelName);

        Map<String, Object> modelData = toMap("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        schemaMetaService.registerMapping(model.getModelName(), "empty.schema.json", 1);

        JsonDynamicModel saved = saveModel(model).block();

        JsonDynamicModel found = service.findById(saved.getId()).block();

        Descriptor idOfTheFoundModel = found.getId();
        assertEquals(saved.getId(), idOfTheFoundModel);

        Map<String, Object> modelData_updated = toMap("{\"field1\":\"bbb\", \"field3\":{\"externalField\":\"bbb\"}, \n" +
                "\"created\":\"blablabla\",\n" +
                "\"modified\": \"blababla\",\n" +
                "\"model\": \"complexModel\",\n" +
                "\"version\": 1}");
        JsonDynamicModel updatedModel = new JsonDynamicModel(idOfTheFoundModel, found.getModelName(), modelData_updated);
        JsonDynamicModel updatedResult = saveModel(updatedModel).block();

        assertEquals(idOfTheFoundModel, updatedResult.getId());
        assertFalse(updatedResult.getModelData().containsKey("_id"));

        JsonDynamicModel foundUpdated = service.findById(idOfTheFoundModel).block();

        assertEquals("bbb", foundUpdated.getModelData().get("field1"));
    }

    // negative tests

    @Test
    void findByIdReturnsException_when_modelNotFound_in_existingCollection() {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        Map<String, Object> modelData = toMap("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        schemaMetaService.registerMapping(modelName, "empty.schema.json", 1);

        saveModel(model).block();

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getModelTypeReactively()).thenReturn(just("complex.schema.json"));
        when(mockDescriptor.getInternalIdReactively()).thenReturn(just(ObjectId.get().toString()));

        Mono<JsonDynamicModel> result = service.findById(mockDescriptor);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void findByIdReturnsException_when_modelNotFound_in_nonExistentCollection() {
        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getModelTypeReactively()).thenReturn(just("non-model-type"));
        when(mockDescriptor.getInternalIdReactively()).thenReturn(just(ObjectId.get().toString()));

        Mono<JsonDynamicModel> result = service.findById(mockDescriptor);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void modelSaved_withFields_create_modified_version__and__without_deleted() {
        NetworkntSchema networkntSchemaMock = mock(NetworkntSchema.class);
        JsonSchema jsonSchemaMock = mock(JsonSchema.class);

        doReturn(Collections.emptySet())
                .when(jsonSchemaMock).validate(any(), any());

        doReturn(jsonSchemaMock)
                .when(networkntSchemaMock).getSchema();

        doReturn(Optional.of(networkntSchemaMock))
                .when(networkntCacheManager).fetchFromCache(anyString());

        Map<String, Object> data = toMap("{\"a\":  \"b\"}");

        JsonDynamicModel model = new JsonDynamicModel("modelName", data);

        schemaMetaService.registerMapping(model.getModelName(), "empty.schema.json", 1);

        JsonDynamicModel saved = saveModel(model).block();

        Object created = saved.getModelData().get(Model.FIELDS.created.name());
        String modified = (String) saved.getModelData().get(Model.FIELDS.model.name());

        assertFalse(saved.getModelData().containsKey(deleted.name()));
        assertNotNull(created);
        assertThat(created, instanceOf(ZonedDateTime.class));
        assertThat(created, instanceOf(ZonedDateTime.class));
        assertNotNull(saved.getModelData().get(Model.FIELDS.version.name()));

        assertDoesNotThrow(() -> parseZonedDateTimeFromISO_8601(modified));

        assertEquals("modelName", saved.getModelData().get(Model.FIELDS.model.name()));
        assertEquals(1L, (long) saved.getModelData().get(Model.FIELDS.version.name()));
    }

    @Test
    void modelUpdated_withFields_modified_changed__version_incremented() {
        NetworkntSchema networkntSchemaMock = mock(NetworkntSchema.class);
        JsonSchema jsonSchemaMock = mock(JsonSchema.class);

        doReturn(Collections.emptySet())
                .when(jsonSchemaMock).validate(any(), any());

        doReturn(jsonSchemaMock)
                .when(networkntSchemaMock).getSchema();

        doReturn(Optional.of(networkntSchemaMock))
                .when(networkntCacheManager).fetchFromCache(anyString());

        Map<String, Object> data = toMap("{\"a\":  \"b\"}");

        JsonDynamicModel model = new JsonDynamicModel("modelName", data);

        schemaMetaService.registerMapping(model.getModelName(), "empty.schema.json", 1);

        JsonDynamicModel saved = saveModel(model).block();

        ZonedDateTime created = (ZonedDateTime) saved.getModelData().get(Model.FIELDS.created.name());
        ZonedDateTime modifiedWhenCreated = (ZonedDateTime) saved.getModelData().get(Model.FIELDS.modified.name());

        assertNotNull(created);
        assertNotNull(modifiedWhenCreated);
        assertNotNull(saved.getModelData().get(Model.FIELDS.version.name()));

        assertEquals("modelName", saved.getModelData().get(Model.FIELDS.model.name()));

        JsonDynamicModel updated = saveModel(saved).block();

        assertEquals(2, (long) updated.getModelData().get(Model.FIELDS.version.name()));

        ZonedDateTime modifiedWhenUpdated = (ZonedDateTime) updated.getModelData().get(Model.FIELDS.modified.name());

        assertTrue(modifiedWhenCreated.isBefore(modifiedWhenUpdated));
    }

    @Test
    void deleteModel_changeDeletedFlagOnly() {
        NetworkntSchema networkntSchemaMock = mock(NetworkntSchema.class);
        JsonSchema jsonSchemaMock = mock(JsonSchema.class);

        doReturn(Collections.emptySet())
                .when(jsonSchemaMock).validate(any(), any());

        doReturn(jsonSchemaMock)
                .when(networkntSchemaMock).getSchema();

        doReturn(Optional.of(networkntSchemaMock))
                .when(networkntCacheManager).fetchFromCache(anyString());

        Map<String, Object> data = toMap("{\"a\":  \"b\"}");

        JsonDynamicModel model = new JsonDynamicModel("dynmodel", data);

        schemaMetaService.registerMapping(model.getModelName(), "empty.schema.json", 1);
        JsonDynamicModel saved = saveModel(model).block();

        JsonDynamicModel removedModel = service.remove(saved.getId()).block();
        assertNotNull(removedModel);

        List<Document> criteria = new ArrayList<>();
        criteria.add(new Document(lineageId.name(), new ObjectId(saved.getId().getInternalId())));
        criteria.add(new Document(deleted.name(), true));

        Flux<Document> presentedInDb = operations.getCollection(model.getModelName())
                .flatMapMany(collection -> {
                    FindPublisher<Document> publisher = collection.find(new Document("$and", criteria));
                    return Flux.from(publisher);
                });

        int removedCount = presentedInDb.collectList().block().size();

        assertEquals(1, removedCount);

        Mono<JsonDynamicModel> findById = service.findById(saved.getId());

        StepVerifier.create(findById)
                .expectError(ModelNotFoundException.class)
                .verify();
    }
}
