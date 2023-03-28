package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBasedUniversalReactiveModelLoadersTest {
    private ListBasedUniversalReactiveModelLoaders loaders;

    @Mock
    private ModelClasses modelClasses;
    @Mock
    private UniversalReactiveModelLoader loader;

    private final Descriptor mongoDescriptor = Descriptor.builder()
            .externalId("externalId")
            .internalId("internalId")
            .modelType("TestModel")
            .storageType(StandardStorageType.MONGO)
            .build();
    private final TestModel modelInDb = new TestModel();

    @BeforeEach
    void createLoaders() {
        loaders = new ListBasedUniversalReactiveModelLoaders(Collections.singletonList(loader), modelClasses);
    }

    @Test
    void whenLoaderSupportsTheDescriptorStorageType_thenTheLoaderShouldBeReturned() {
        when(loader.type()).thenReturn(StandardStorageType.MONGO);
        when(modelClasses.getClassByModelName("TestModel"))
                .thenReturn(modelClass(TestModel.class));
        when(loader.loadByInternalId("internalId", TestModel.class))
                .thenReturn(Mono.just(modelInDb));

        Mono<Model> modelMono = loaders.loadByDescriptor(mongoDescriptor);

        assertThat(modelMono.block(), is(sameInstance(modelInDb)));
    }

    @SuppressWarnings("SameParameterValue")
    private Class<Model> modelClass(Class<? extends Model> modelClass) {
        @SuppressWarnings("unchecked") Class<Model> castClass = (Class<Model>) modelClass;
        return castClass;
    }

    @Test
    void whenLoaderDoesNotSupportTheDescriptorStorageType_thenAnExceptionShouldBeThrown() {
        when(loader.type()).thenReturn(StandardStorageType.POSTGRES);

        try {
            //noinspection UnassignedFluxMonoInstance
            loaders.loadByDescriptor(mongoDescriptor);
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("No loader supports storage type 'mongo'. " +
                    "Make sure you have an instance of UniversalReactiveModelLoader that supports 'mongo' " +
                    "in the application context."));
        }
    }

    private static class TestModel implements Model {
    }
}