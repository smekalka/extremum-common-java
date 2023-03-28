package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescriptorTest {
    private DescriptorLoader oldDescriptorLoader;

    @Mock
    private DescriptorLoader descriptorLoader;

    private final Descriptor descriptorInDb = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .storageType(StandardStorageType.MONGO)
            .modelType("modelType")
            .build();

    private final Descriptor collectionDescriptorInDb = Descriptor.forCollection("external-id",
            CollectionDescriptor.forFree("free"));

    @BeforeEach
    void initDescriptorLoader() {
        //noinspection deprecation
        oldDescriptorLoader = StaticDescriptorLoaderAccessor.getDescriptorLoader();
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

    @AfterEach
    void restoreDescriptorLoader() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(oldDescriptorLoader);
    }

    @Test
    void givenInternalIdIsNotNull_whenGetInternalIdReactivelyIsUsed_thenTheInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> internalIdMono = descriptor.getInternalIdReactively();

        StepVerifier.create(internalIdMono)
                .expectNext("internal-id")
                .verifyComplete();
    }

    @Test
    void givenInternalIdIsNull_whenGetInternalIdReactivelyIsUsed_thenTheInternalIdShouldBeFilledAndReturned() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> internalIdMono = descriptor.getInternalIdReactively();

        StepVerifier.create(internalIdMono)
                .expectNext("internal-id")
                .verifyComplete();

        assertThat(descriptor.getInternalId(), is("internal-id"));
    }

    @Test
    void givenInternalIdIsNullAndNoDescriptorExistsInDB_whenGetInternalIdReactivelyIsUsed_thenAnErrorShouldBeReturned() {
        when(descriptorLoader.loadByExternalIdReactively(anyString()))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> internalIdMono = descriptor.getInternalIdReactively();

        StepVerifier.create(internalIdMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal ID was not found for external ID external-id"));
                })
                .verify();
    }

    @Test
    void givenExternalIdIsNotNull_whenGetExternalIdReactivelyIsUsed_thenTheExternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> externalIdMono = descriptor.getExternalIdReactively();

        StepVerifier.create(externalIdMono)
                .expectNext("external-id")
                .verifyComplete();
    }

    @Test
    void givenExternalIdIsNull_whenGetExternalIdReactivelyIsUsed_thenTheExternalIdShouldBeFilledAndReturned() {
        when(descriptorLoader.loadByInternalIdReactively("internal-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> externalIdMono = descriptor.getExternalIdReactively();

        StepVerifier.create(externalIdMono)
                .expectNext("external-id")
                .verifyComplete();

        assertThat(descriptor.getExternalId(), is("external-id"));
    }

    @Test
    void givenExternalIdIsNullAndNoDescriptorExistsInDB_whenGetExternalIdReactivelyIsUsed_thenErrorShouldBeReturned() {
        when(descriptorLoader.loadByInternalIdReactively(anyString()))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> externalIdMono = descriptor.getExternalIdReactively();

        StepVerifier.create(externalIdMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal id internal-id without corresponding descriptor"));
                })
                .verify();
    }

    @Test
    void givenStorageTypeIsNotNull_whenGetStorageTypeReactivelyIsUsed_thenTheStorageTypeShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .storageType(StandardStorageType.MONGO)
                .build();

        Mono<String> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectNext("mongo")
                .verifyComplete();
    }

    @Test
    void givenStorageTypeIsNullAndExternalIdIsNotNull_whenGetStorageTypeReactivelyIsUsed_thenTheStorageTypeShouldBeReturnedAndFilled() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectNext("mongo")
                .verifyComplete();

        assertThat(descriptor.getStorageType(), is("mongo"));
    }

    @Test
    void givenStorageTypeIsNullAndExternalIdIsNotNullAndNoDescriptorIsFound_whenGetStorageTypeReactivelyIsUsed_thenAnExceptionShouldBeThrown() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal ID was not found for external ID external-id"));
                })
                .verify();
    }

    @Test
    void givenStorageTypeIsNullAndInternalIdIsNotNull_whenGetStorageTypeReactivelyIsUsed_thenTheStorageTypeShouldBeReturnedAndFilled() {
        when(descriptorLoader.loadByInternalIdReactively("internal-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectNext("mongo")
                .verifyComplete();

        assertThat(descriptor.getStorageType(), is("mongo"));
    }

    @Test
    void givenStorageTypeIsNullAndInternalIdIsNotNullAndNoDescriptorIsFound_whenGetStorageTypeReactivelyIsUsed_thenAnExceptionShouldBeThrown() {
        when(descriptorLoader.loadByInternalIdReactively("internal-id"))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal id internal-id without corresponding descriptor"));
                })
                .verify();
    }

    @Test
    void givenTypeIsNotNull_whenEffectiveTypeIsUsed_thenTheTypeShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .type(Descriptor.Type.COLLECTION)
                .build();

        assertThat(descriptor.effectiveType(), is(Descriptor.Type.COLLECTION));
    }

    @Test
    void givenTypeIsNullAndExternalIdIsNotNull_whenEffectiveTypeIsUsed_thenTheEffectiveTypeShouldBeReturnedAndFilled() {
        when(descriptorLoader.loadByExternalId("external-id"))
                .thenReturn(Optional.of(collectionDescriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        assertThat(descriptor.effectiveType(), is(Descriptor.Type.COLLECTION));

        assertThat(descriptor.getType(), is(Descriptor.Type.COLLECTION));
    }

    @Test
    void givenTypeIsNullAndExternalIdIsNotNullAndNoDescriptorIsFound_whenTypeReactivelyIsUsed_thenAnExceptionShouldBeThrown() {
        when(descriptorLoader.loadByExternalId("external-id"))
                .thenReturn(Optional.empty());

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        assertThrows(DescriptorNotFoundException.class, descriptor::effectiveType);
    }

    @Test
    void givenTypeIsNotNull_whenEffectiveTypeReactivelyIsUsed_thenTheTypeShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .type(Descriptor.Type.COLLECTION)
                .build();

        Mono<Descriptor.Type> typeMono = descriptor.effectiveTypeReactively();

        StepVerifier.create(typeMono)
                .expectNext(Descriptor.Type.COLLECTION)
                .verifyComplete();
    }

    @Test
    void givenTypeIsNullAndExternalIdIsNotNull_whenEffectiveTypeReactivelyIsUsed_thenTheEffectiveTypeShouldBeReturnedAndFilled() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(collectionDescriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<Descriptor.Type> typeMono = descriptor.effectiveTypeReactively();

        StepVerifier.create(typeMono)
                .expectNext(Descriptor.Type.COLLECTION)
                .verifyComplete();

        assertThat(descriptor.effectiveType(), is(Descriptor.Type.COLLECTION));
    }

    @Test
    void givenTypeIsNullAndExternalIdIsNotNullAndNoDescriptorIsFound_whenEffectiveTypeReactivelyIsUsed_thenAnExceptionShouldBeThrown() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<Descriptor.Type> typeMono = descriptor.effectiveTypeReactively();

        StepVerifier.create(typeMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal ID was not found for external ID external-id"));
                })
                .verify();
    }

    @Test
    void givenNoExternalIdIsDefined_whenCallingHasExternalId_thenFalseShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        assertThat(descriptor.hasExternalId(), is(false));
    }

    @Test
    void givenExternalIdIsDefined_whenCallingHasExternalId_thenFalseShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        assertThat(descriptor.hasExternalId(), is(true));
    }

    @Test
    void givenNoInternalIdIsDefined_whenCallingHasExternalId_thenFalseShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        assertThat(descriptor.hasInternalId(), is(false));
    }

    @Test
    void givenInternalIdIsDefined_whenCallingHasExternalId_thenFalseShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        assertThat(descriptor.hasInternalId(), is(true));
    }

    @Test
    void getModelTypeReactivelyTest() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .storageType(StandardStorageType.MONGO)
                .build();

        when(descriptorLoader.loadByInternalIdReactively("internal-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Mono<String> modelTypeResult = descriptor.getModelTypeReactively();

        StepVerifier.create(modelTypeResult)
                .expectNext("modelType")
                .verifyComplete();
    }
}
