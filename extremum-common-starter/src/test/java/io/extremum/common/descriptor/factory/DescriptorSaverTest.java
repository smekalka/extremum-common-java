package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DescriptorSaverTest {
    @InjectMocks
    private DescriptorSaver descriptorSaver;
    @Mock
    private DescriptorService descriptorService;

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    @Test
    void whenCreatingADescriptor_thenCorrectDataShouldBeSavedWithDescriptorService() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        descriptorSaver.createAndSave("internal-id", "Test", StandardStorageType.MONGO, "iri", Collections.emptyMap());

        verify(descriptorService).store(descriptorCaptor.capture());
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