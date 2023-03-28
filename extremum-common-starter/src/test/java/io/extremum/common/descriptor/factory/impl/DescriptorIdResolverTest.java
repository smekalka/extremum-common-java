package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DescriptorIdResolverTest {

    @InjectMocks
    private DescriptorIdResolver descriptorIdResolver;

    @Mock
    private DescriptorService descriptorService;

    @Test
    public void whenDescriptorHasOnlyInternalIdAndExternalIdExistsInDB_resolverShouldResolveExternalId() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = createDescriptor(internalId);

        String externalId = UUID.randomUUID().toString();
        when(descriptorService.loadByInternalId(internalId)).thenReturn(Optional.of(createDescriptor(internalId, externalId)));

        Descriptor resolved = descriptorIdResolver.resolveIds(descriptor);

        assertThat(resolved).isNotNull();
        assertThat(resolved.hasExternalId()).isTrue();
        assertThat(resolved.getExternalId()).isEqualTo(externalId);
        verify(descriptorService).loadByInternalId(internalId);
    }

    @Test
    public void whenDescriptorHasOnlyInternalIdAndExternalIdDoesntExistInDB_resolverShouldThrowException() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = createDescriptor(internalId);

        when(descriptorService.loadByInternalId(internalId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(DescriptorNotFoundException.class)
                .isThrownBy(() -> descriptorIdResolver.resolveIds(descriptor))
                .withMessage("External ID was not found for internal ID " + internalId);
        verify(descriptorService).loadByInternalId(internalId);

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.hasExternalId()).isFalse();
    }

    private Descriptor createDescriptor(String internalId) {
        return Descriptor.builder()
                .internalId(internalId)
                .storageType(StandardStorageType.POSTGRES)
                .build();
    }

    private Descriptor createDescriptor(String internalId, String externalId) {
        return Descriptor.builder()
                .internalId(internalId)
                .storageType(StandardStorageType.POSTGRES)
                .externalId(externalId)
                .build();
    }

}