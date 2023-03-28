package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultDynamicModelMetadataProviderService;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.impl.DefaultJsonBasedDynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntJsonDynamicModelValidator;
import io.extremum.dynamic.watch.DefaultDynamicModelWatchService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static io.atlassian.fugue.Try.successful;
import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(classes = JsonBasedDynamicModelServiceTestConfiguration.class)
class DefaultJsonBasedDynamicModelServiceTest {
    @SpyBean
    NetworkntJsonDynamicModelValidator modelValidator;

    @Autowired
    DateTypesNormalizer normalizer;

    @Autowired
    DatesProcessor datesProcessor;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    SchemaMetaService schemaMetaService;

    @MockBean
    JsonDynamicModelDao dao;

    @MockBean
    DefaultDynamicModelMetadataProviderService metadataProvider;

    @MockBean
    DefaultDynamicModelWatchService watchService;

    @Captor
    ArgumentCaptor<JsonDynamicModel> modelCaptor;

    @Test
    void saveModel() {
        JsonDynamicModel model = new JsonDynamicModel("AModel", new HashMap<>());

        schemaMetaService.registerMapping(model.getModelName(), "empty.schema.json", 1);

        doReturn(just(successful(mock(ValidationContext.class)))).when(modelValidator).validate(any());

        when(dao.create(any(), anyString())).thenReturn(just(model));
        when(watchService.registerSaveOperation(any())).thenReturn(empty());

        DefaultJsonBasedDynamicModelService service = new DefaultJsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor, watchService);

        Mono<JsonDynamicModel> saved = service.saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(resultModel -> {
                    assertEquals(resultModel.getId(), model.getId());
                    assertEquals(resultModel.getModelName(), model.getModelName());
                })
                .verifyComplete();
        verify_Validator_HasAccept_Model_1_times(model);
        verify_Normalizer_HasAccept_Model_1_times();
        verify_DynamicModelDao_HasAccept_Model_1_times(model);

        verify(watchService, times(1)).registerSaveOperation(any());
    }

    @Test
    void notValidModelIsNotSaves() {
        DefaultJsonBasedDynamicModelService service = new DefaultJsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor, watchService);

        Map<String, Object> invalidModelRawValue = new HashMap<>();
        invalidModelRawValue.put("field1", 1);

        JsonDynamicModel model = new JsonDynamicModel("model", invalidModelRawValue);

        schemaMetaService.registerMapping(model.getModelName(), "simple.schema.json", 1);

        Mono<JsonDynamicModel> result = service.saveModel(model);

        verify(dao, Mockito.never()).create(any(), anyString());

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .expectError(DynamicModelValidationException.class)
                .verify();

        verify(watchService, Mockito.never()).registerSaveOperation(any());
    }

    @Test
    void modelIsNotSaved_schemaDoesntExists() {
        DefaultJsonBasedDynamicModelService service = new DefaultJsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor, watchService);

        String unknownModel = "unknownModel";

        JsonDynamicModel jModel = new JsonDynamicModel(unknownModel, null);

        schemaMetaService.registerMapping(jModel.getModelName(), "unknown_schema", 1);

        Mono<JsonDynamicModel> result = service.saveModel(jModel);

        verify(dao, Mockito.never()).create(any(), anyString());

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .expectError(SchemaLoadingException.class)
                .verify();

        verify(watchService, Mockito.never()).registerSaveOperation(any());
    }

    @Test
    void removeModel_callWatch_Test() {
        DefaultJsonBasedDynamicModelService service = new DefaultJsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor, watchService);

        Descriptor id = Descriptor.builder()
                .internalId("int")
                .externalId("ext")
                .storageType(StandardStorageType.MONGO)
                .modelType("model")
                .build();

        JsonDynamicModel jModel = mock(JsonDynamicModel.class);
        doReturn(just(jModel)).when(dao).getByIdFromCollection(id, "model");
        doReturn(just(jModel)).when(dao).remove(id, "model");
        when(watchService.registerDeleteOperation(any())).thenReturn(empty());

        service.remove(id).block();

        verify(dao, times(1)).remove(id, "model");
        verify(watchService, times(1)).registerDeleteOperation(any());
    }

    private void verify_Normalizer_HasAccept_Model_1_times() {
        verify(normalizer, times(1)).normalize(any(), anyCollection());
    }

    private void verify_DynamicModelDao_HasAccept_Model_1_times(JsonDynamicModel model) {
        verify(dao, times(1)).create(modelCaptor.capture(), anyString());

        JsonDynamicModel capturedModel = modelCaptor.getValue();

        assertEquals(model.getId(), capturedModel.getId());
        assertEquals(model.getModelName(), capturedModel.getModelName());

        capturedModel.getModelData().remove(created.name());
        capturedModel.getModelData().remove(modified.name());
        capturedModel.getModelData().remove(Model.FIELDS.model.name());
        capturedModel.getModelData().remove(version.name());
        assertEquals(model.getModelData(), capturedModel.getModelData());
    }

    private void verify_Validator_HasAccept_Model_1_times(JsonDynamicModel model) {
        verify(modelValidator, times(1)).validate(modelCaptor.capture());

        assertEquals(model, modelCaptor.getValue());
    }
}
