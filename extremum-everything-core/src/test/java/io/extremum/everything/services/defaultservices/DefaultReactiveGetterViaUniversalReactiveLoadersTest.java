package io.extremum.everything.services.defaultservices;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultReactiveGetterViaUniversalReactiveLoadersTest {
    @InjectMocks
    private DefaultReactiveGetterViaUniversalReactiveLoaders getter;

    @Mock
    private UniversalReactiveModelLoaders universalReactiveModelLoaders;
    @Mock
    private ReactiveDescriptorService reactiveDescriptorService;

    private final TestModel modelFromDatabase = new TestModel();
    private final Descriptor descriptor = Descriptor.builder()
            .externalId("externalId")
            .internalId("internalId")
            .modelType("TestModel")
            .storageType(StandardStorageType.MONGO)
            .build();

    @Test
    void whenGettingReactively_thenTheResultIsObtainedViaCommonService() {
        when(reactiveDescriptorService.loadByInternalId("internalId"))
                .thenReturn(Mono.just(descriptor));
        when(universalReactiveModelLoaders.loadByDescriptor(descriptor))
                .thenReturn(Mono.just(modelFromDatabase));

        Model model = getter.get("internalId").block();

        assertThat(model, is(sameInstance(modelFromDatabase)));
    }

    private static class TestModel implements Model {
    }
}