package io.extremum.graphql.resolver;

import graphql.schema.DataFetchingEnvironment;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.everything.services.defaultservices.DefaultRemover;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractGraphQLMutationResolverTest {

    static {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(new DescriptorLoader() {
            @Override
            public Optional<Descriptor> loadByExternalId(String externalId) {
                return Optional.of(Descriptor.builder().internalId("does-not-matter").externalId(externalId).build());
            }

            @Override
            public Optional<Descriptor> loadByInternalId(String internalId) {
                return Optional.empty();
            }

            @Override
            public Mono<Descriptor> loadByExternalIdReactively(String externalId) {
                return null;
            }

            @Override
            public Mono<Descriptor> loadByInternalIdReactively(String internalId) {
                return null;
            }
        });
    }

    private final TestModel existentModel = new TestModel();
    private final TestModel savedModel = new TestModel();
    @Mock
    private ModelSaver modelSaver;
    @Mock
    private ModelRetriever modelRetriever;
    @Mock
    private DataSecurity dataSecurity;
    @Mock
    private DefaultRemover defaultRemover;
    @Mock
    private DataFetchingEnvironment environment;
    private AbstractGraphQLMutationResolver service;

    @BeforeEach
    private void setUp() {
        service = new AbstractGraphQLMutationResolver(modelSaver, modelRetriever, dataSecurity, defaultRemover) {
        };
        lenient().when(modelRetriever.retrieveModel(any())).thenReturn(existentModel);
        lenient().when(modelSaver.saveModel(any())).thenReturn(savedModel);
        lenient().when(environment.getMergedField()).thenReturn(null);
    }

    @Test
    @DisplayName("Returns existing model when id provided and input is null")
    void returns_existing_model_when_id_provided_and_input_is_null() {
        Assertions.assertEquals(service.updateOrCreate("does-not-matter", null, environment), existentModel);
        verify(modelRetriever, times(1)).retrieveModel(any());
        verify(dataSecurity, times(1)).checkGetAllowed(existentModel);
    }

    @Test
    @DisplayName("Returns saved model when id is null and input is provider")
    void returns_existing_model_when_id_is_null_and_input_is_provided() {
        Assertions.assertEquals(service.updateOrCreate(null, new TestModel(), environment), savedModel);
        verify(modelSaver, times(1)).saveModel(any());
        verify(dataSecurity, times(1)).checkCreateAllowed(any());
    }

    @Test
    @DisplayName("Returns updated model when id is provided and input is provider")
    void returns_updated_model_when_id_is_provided_and_input_is_provided() {
        lenient().when(modelRetriever.retrieveModel(any())).thenReturn(savedModel);
        TestModel update = new TestModel();
        update.setField("new value");
        Assertions.assertEquals(service.updateOrCreate("does-not-matter", update, environment).getField(), "new value");
        verify(modelSaver, times(1)).saveModel(any());
        verify(modelRetriever, times(1)).retrieveModel(any());
        verify(dataSecurity, times(1)).checkGetAllowed(any());
        verify(dataSecurity, times(1)).checkPatchAllowed(any());
    }

    @Test
    @DisplayName("Removes model")
    void removes_model() {

        service.delete("does-not-matter");
        verify(modelRetriever, times(1)).retrieveModel(any());
        verify(defaultRemover, times(1)).remove(anyString());
        verify(dataSecurity, times(1)).checkRemovalAllowed(existentModel);
    }

    @NoArgsConstructor
    public static class TestModel implements BasicModel<UUID> {

        private String field;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public void setId(UUID uuid) {

        }

        @Override
        public Descriptor getUuid() {
            return null;
        }

        @Override
        public void setUuid(Descriptor uuid) {

        }

        @Override
        public String getIri() {
            return null;
        }

        @Override
        public void setIri(String iri) {

        }
    }
}