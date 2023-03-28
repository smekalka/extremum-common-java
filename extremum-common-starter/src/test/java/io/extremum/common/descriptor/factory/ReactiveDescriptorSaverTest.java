package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static io.extremum.test.mockito.ReturnFirstArgInMono.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ReactiveDescriptorSaverTest {
    @InjectMocks
    private ReactiveDescriptorSaver descriptorSaver;
    @Mock
    private DescriptorService descriptorService;
    @Mock
    private ReactiveDescriptorService reactiveDescriptorService;

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    @Test
    void whenCreatingADescriptorReactiely_thenCorrectDataShouldBeSavedWithReactiveDescriptorService() {
        when(descriptorService.createExternalId()).thenReturn("external-id");
        when(reactiveDescriptorService.loadByInternalId("internal-id")).thenReturn(Mono.empty());
        when(reactiveDescriptorService.store(any())).then(returnFirstArgInMono());

        descriptorSaver.createAndSave("internal-id", "Test", StandardStorageType.MONGO, "iri").block();

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorService).store(descriptorCaptor.capture());
        Descriptor savedDescriptor = descriptorCaptor.getValue();

        assertThatSavedDescriptorWithCorrectData(savedDescriptor);
    }

    private void assertThatSavedDescriptorWithCorrectData(Descriptor savedDescriptor) {
        assertThat(savedDescriptor.getExternalId(), is("external-id"));
        assertThat(savedDescriptor.getInternalId(), is("internal-id"));
        assertThat(savedDescriptor.getModelType(), is("Test"));
        assertThat(savedDescriptor.getStorageType(), is("mongo"));
    }
}