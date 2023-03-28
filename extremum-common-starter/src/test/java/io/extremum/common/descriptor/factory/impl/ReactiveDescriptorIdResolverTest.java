package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReactiveDescriptorIdResolverTest {

    @InjectMocks
    private ReactiveDescriptorIdResolver descriptorIdResolver;

    @Mock
    private ReactiveDescriptorService descriptorService;

    @Test
    public void whenDescriptorHasOnlyInternalIdAndExternalIdExistsInDB_resolverShouldResolveExternalId() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = createDescriptor(internalId);

        String externalId = UUID.randomUUID().toString();
        when(descriptorService.loadByInternalId(eq(internalId))).thenReturn(Mono.just(createDescriptor(internalId, externalId)));

        StepVerifier.create(descriptorIdResolver.resolveIds(descriptor))
                .assertNext(resolved -> {
                    assertThat(resolved).isNotNull();
                    assertThat(resolved.hasExternalId()).isTrue();
                    assertThat(resolved.getExternalId()).isEqualTo(externalId);
                })
                .verifyComplete();

        verify(descriptorService).loadByInternalId(internalId);
    }

    @Test
    public void whenDescriptorHasOnlyInternalIdAndExternalIdDoesntExistInDB_resolverShouldThrowException() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = createDescriptor(internalId);

        when(descriptorService.loadByInternalId(eq(internalId))).thenReturn(Mono.empty());

        StepVerifier.create(descriptorIdResolver.resolveIds(descriptor))
                .expectErrorMatches(error -> error instanceof DescriptorNotFoundException &&
                        error.getMessage().contains("External ID was not found for internal ID " + internalId))
                .verify();

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