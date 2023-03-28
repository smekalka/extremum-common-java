package io.extremum.everything.services.management;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToRequestDtoConverter;
import io.extremum.common.dto.converters.StubDtoConverter;
import io.extremum.common.dto.converters.services.DefaultDtoConversionService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.dto.converters.services.DtoConvertersCollection;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.annotation.ModelName;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.security.AllowEverythingForDataAccessReactively;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.test.core.MockedMapperDependencies;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static io.extremum.test.mockito.ReturnFirstArgInMono.returnFirstArgInMono;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ReactivePatchFlowImplTest {
    private static final String BEFORE_PATCHING = "Before patching";
    private static final String AFTER_PATCHING = "After patching";

    private ReactivePatchFlowImpl patchFlow;

    @Mock
    private ModelRetriever modelRetriever;
    @Mock
    private ReactiveModelSaver modelSaver;
    @Spy
    private DtoConversionService dtoConversionService = new DefaultDtoConversionService(
            new DtoConvertersCollection(
                    singletonList(new TestModelDtoConverter()), emptyList(),
                    emptyList(), emptyList(), singletonList(new TestModelDtoConverter())
            ),
            new StubDtoConverter(), new ObjectMapper()
    );
    @Spy
    private ObjectMapper objectMapper = new SystemJsonObjectMapper(new MockedMapperDependencies());
    @Spy
    private EmptyFieldDestroyer emptyFieldDestroyer = new PublicEmptyFieldDestroyer();
    @Mock
    private RequestDtoValidator requestDtoValidator;
    @Spy
    private ReactiveDataSecurity dataSecurity = new AllowEverythingForDataAccessReactively();
    @Spy
    private PatcherHooksCollection patcherHooksCollection = new PatcherHooksCollection(emptyList());

    @Captor
    private ArgumentCaptor<TestModel> testModelCaptor;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType(TestModel.MODEL_NAME)
            .storageType(StandardStorageType.MONGO)
            .build();

    @BeforeEach
    void createPatcherFlow() {
        ReactivePatcher patcher = new ReactivePatcherImpl(dtoConversionService, objectMapper,
                emptyFieldDestroyer, requestDtoValidator, patcherHooksCollection);
        patchFlow = new ReactivePatchFlowImpl(modelRetriever, patcher, modelSaver,
                dataSecurity, patcherHooksCollection);
    }

    @BeforeEach
    void setupMocks() {
        whenSaveAModelThenReturnIt();
    }

    private void whenSaveAModelThenReturnIt() {
        //noinspection UnassignedFluxMonoInstance
        lenient().doAnswer(returnFirstArgInMono())
                .when(modelSaver).saveModel(any());
    }

    @Test
    void whenPatching_thenPatchedModelShouldBeSaved() throws Exception {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);

        patchFlow.patch(descriptor, patchToChangeNameTo(AFTER_PATCHING)).block();

        assertThatSavedModelWithNewName(AFTER_PATCHING);
    }

    @SuppressWarnings("SameParameterValue")
    private void whenRetrieveModelThenReturnTestModelWithName(String name) {
        TestModel model = new TestModel();
        model.name = name;

        when(modelRetriever.retrieveModelReactively(descriptor)).thenReturn(Mono.just(model));
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private JsonPatch patchToChangeNameTo(String newName) throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode(newName));
        return new JsonPatch(Collections.singletonList(operation));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertThatSavedModelWithNewName(String newName) {
        //noinspection UnassignedFluxMonoInstance
        verify(modelSaver).saveModel(testModelCaptor.capture());
        assertThat(testModelCaptor.getValue(), hasProperty("name", equalTo(newName)));
    }

    @Test
    void whenPatching_thenReturnedModelShouldBeThePatchedOne() throws Exception {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);

        Model patchedModel = patchFlow.patch(descriptor, patchToChangeNameTo(AFTER_PATCHING)).block();

        assertThat(patchedModel, instanceOf(TestModel.class));
        assertThat(patchedModel, hasProperty("name", equalTo(AFTER_PATCHING)));
    }

    @Test
    void givenDataSecurityDoesNotAllowToPatch_whenPatching_thenAnExceptionShouldBeThrown() {
        whenRetrieveModelThenReturnATestModel();
        //noinspection UnassignedFluxMonoInstance
        doThrow(new ExtremumAccessDeniedException("Access denied"))
                .when(dataSecurity).checkPatchAllowed(any());

        try {
            patchFlow.patch(descriptor, anyPatch()).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    private void whenRetrieveModelThenReturnATestModel() {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);
    }

    @NotNull
    private JsonPatch anyPatch() {
        return new JsonPatch(emptyList());
    }

    @Test
    void givenPatcherHooksExist_whenPatching_thenAllTheHookMethodsShouldBeCalled() {
        whenRetrieveModelThenReturnATestModel();

        patchFlow.patch(descriptor, anyPatch()).block();

        verify(patcherHooksCollection).afterPatchAppliedToDto(eq(TestModel.MODEL_NAME), any(), any());
        verify(patcherHooksCollection).beforeSave(eq(TestModel.MODEL_NAME), any());
        verify(patcherHooksCollection).afterSave(eq(TestModel.MODEL_NAME), any());
    }

    @Test
    void whenPatching_thenEmptyFieldDestroyerShouldBeApplied() {
        whenRetrieveModelThenReturnATestModel();

        patchFlow.patch(descriptor, anyPatch()).block();

        verify(emptyFieldDestroyer).destroy(any());
    }
    
    @ModelName(TestModel.MODEL_NAME)
    @ToString
    @Getter
    public static class TestModel extends MongoCommonModel {
        private static final String MODEL_NAME = "TestModel";

        private String name;
    }

    private static class TestModelRequestDto implements RequestDto {
        @JsonProperty
        private String name;
    }

    private static class TestModelDtoConverter
            implements ReactiveToRequestDtoConverter<TestModel, TestModelRequestDto>,
            FromRequestDtoConverter<TestModel, TestModelRequestDto> {

        @Override
        public Mono<TestModelRequestDto> convertToRequestReactively(TestModel model, ConversionConfig config) {
            TestModelRequestDto dto = new TestModelRequestDto();
            dto.name = model.name;
            return Mono.just(dto);
        }

        @Override
        public TestModel convertFromRequest(TestModelRequestDto dto) {
            TestModel model = new TestModel();
            model.name = dto.name;
            return model;
        }

        @Override
        public Class<? extends TestModelRequestDto> getRequestDtoType() {
            return TestModelRequestDto.class;
        }

        @Override
        public String getSupportedModel() {
            return TestModel.MODEL_NAME;
        }
    }

}