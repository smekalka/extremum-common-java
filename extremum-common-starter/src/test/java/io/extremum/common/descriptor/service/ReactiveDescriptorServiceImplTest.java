package io.extremum.common.descriptor.service;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.mongo.facilities.DescriptorIsAlreadyReadyException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static io.extremum.test.mockito.ReturnFirstArgInMono.returnFirstArgInMono;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveDescriptorServiceImplTest {
    @InjectMocks
    private ReactiveDescriptorServiceImpl reactiveDescriptorService;

    @Mock
    private ReactiveDescriptorDao reactiveDescriptorDao;

    private final Descriptor descriptor = new Descriptor("external-id");

    @Test
    void givenDescriptorIsOk_whenStoring_thenItShouldBeStoredToDao() {
        when(reactiveDescriptorDao.store(descriptor)).thenReturn(Mono.just(descriptor));

        reactiveDescriptorService.store(descriptor).block();

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorDao).store(descriptor);
    }

    @Test
    void givenDescriptorIsNull_whenStoring_thenItShouldBeStoredToDao() {
        try {
            //noinspection UnassignedFluxMonoInstance
            reactiveDescriptorService.store(null);
            fail("An exception should be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Descriptor is null"));
        }
    }

    @Test
    void whenLoadingByExternalId_thenDescriptorShouldBeLoadedFromDao() {
        when(reactiveDescriptorDao.retrieveByExternalId("externalId")).thenReturn(Mono.just(descriptor));

        Mono<Descriptor> mono = reactiveDescriptorService.loadByExternalId("externalId");

        assertThat(mono.block(), is(sameInstance(descriptor)));
    }

    @Test
    void whenLoadingByInternalId_thenDescriptorShouldBeLoadedFromDao() {
        when(reactiveDescriptorDao.retrieveByInternalId("internalId")).thenReturn(Mono.just(descriptor));

        Mono<Descriptor> mono = reactiveDescriptorService.loadByInternalId("internalId");

        assertThat(mono.block(), is(sameInstance(descriptor)));
    }

    @Test
    void givenDescriptorIsBlank_whenLoadingItByExternalId_thenDescriptorNotReadyExceptionShouldBeThrown() {
        when(reactiveDescriptorDao.retrieveByExternalId("external-id"))
                .thenReturn(Mono.just(blankDescriptor()));

        Mono<Descriptor> mono = reactiveDescriptorService.loadByExternalId("external-id");

        StepVerifier.create(mono)
                .expectError(DescriptorNotReadyException.class)
                .verify();
    }

    private Descriptor blankDescriptor() {
        return Descriptor.builder()
                .externalId("external-id")
                .storageType(StandardStorageType.MONGO)
                .readiness(Descriptor.Readiness.BLANK)
                .build();
    }

    @Test
    void whenLoadingDescriptorsByInternalIds_thenThenShouldBeLoadedFromDao() {
        when(reactiveDescriptorDao.retrieveMapByInternalIds(singletonList("internal-id")))
                .thenReturn(Mono.just(singletonMap("internal-id", "external-id")));

        Mono<Map<String, String>> mono = reactiveDescriptorService.loadMapByInternalIds(singletonList("internal-id"));

        StepVerifier.create(mono)
                .expectNext(singletonMap("internal-id", "external-id"))
                .verifyComplete();
    }

    @Test
    void givenABlankDescriptorExists_whenMakingItReady_thenItBecomesReadyAndReturnsTheDescriptor() {
        Descriptor blankDescriptor = Descriptor.builder()
                .externalId("external-id")
                .readiness(Descriptor.Readiness.BLANK)
                .storageType(StandardStorageType.MONGO)
                .build();
        when(reactiveDescriptorDao.retrieveByExternalId("external-id"))
                .thenReturn(Mono.just(blankDescriptor));
        when(reactiveDescriptorDao.store(any()))
                .then(returnFirstArgInMono());

        Mono<Descriptor> mono = reactiveDescriptorService.makeDescriptorReady("external-id", "TestModel");

        StepVerifier.create(mono)
                .assertNext(descriptor -> {
                    assertThat(descriptor.getReadiness(), is(Descriptor.Readiness.READY));
                    assertThat(descriptor.getModelType(), is("TestModel"));
                })
                .verifyComplete();

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorDao).store(blankDescriptor);
    }

    @Test
    void givenADescriptorIsReady_whenMakingItReady_thenExceptionShouldBeThrown() {
        Descriptor readyDescriptor = Descriptor.builder()
                .externalId("external-id")
                .readiness(Descriptor.Readiness.READY)
                .storageType(StandardStorageType.MONGO)
                .build();
        when(reactiveDescriptorDao.retrieveByExternalId("external-id"))
                .thenReturn(Mono.just(readyDescriptor));

        Mono<Descriptor> mono = reactiveDescriptorService.makeDescriptorReady("external-id", "TestModel");

        StepVerifier.create(mono)
                .expectError(DescriptorIsAlreadyReadyException.class)
                .verify();
    }

    @Test
    void shouldRelayDestroyRequestToDao() {
        when(reactiveDescriptorDao.destroy("external-id")).thenReturn(Mono.empty());

        reactiveDescriptorService.destroyDescriptor("external-id").block();

        verify(reactiveDescriptorDao).destroy("external-id");
    }

    @Test
    void shouldNotGoToDaoWhenCollectionOfInternalIdsToLoadIsEmpty() {
        reactiveDescriptorService.loadMapByInternalIds(emptyList()).block();

        verify(reactiveDescriptorDao, never()).retrieveMapByInternalIds(any());
    }

    @Test
    void whenLoadingByIri_thenDescriptorShouldBeLoadedFromDao() {
        when(reactiveDescriptorDao.retrieveByIri("\\/a\\/b\\/c")).thenReturn(Mono.just(descriptor));

        Mono<Descriptor> mono = reactiveDescriptorService.loadByIri("\\/a\\/b\\/c");

        assertThat(mono.block(), is(sameInstance(descriptor)));
    }
}