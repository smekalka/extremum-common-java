package io.extremum.common.collection.service;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

import static io.extremum.test.mockito.ReturnFirstArgInMono.returnFirstArgInMono;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveCollectionDescriptorServiceImplTest {
    private ReactiveCollectionDescriptorServiceImpl service;

    @Mock
    private ReactiveDescriptorDao reactiveDescriptorDao;
    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private ReactiveCollectionDescriptorExtractor collectionDescriptorExtractor =
            new ReactiveCollectionOverridesWithDescriptorExtractorList(emptyList());

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    private final CollectionDescriptor owned = CollectionDescriptor.forOwned(new Descriptor("host-id"), "items");
    private final Descriptor collDescriptorInDb = Descriptor.forCollection("externalId", owned);

    @BeforeEach
    void createService() {
        service = new ReactiveCollectionDescriptorServiceImpl(reactiveDescriptorDao, descriptorService,
                collectionDescriptorExtractor);
    }

    @Test
    void whenRetrievingACollectionByExternalId_thenItShouldBeRetrievedFromDao() {
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collDescriptorInDb));

        Mono<CollectionDescriptor> mono = service.retrieveByExternalId("externalId");

        assertThat(mono.block(), is(sameInstance(collDescriptorInDb.getCollection())));
    }

    @Test
    void givenDescriptorTypeIsNotCollection_whenRetrievingACollectionByExternalId_thenItShouldBeRetrievedFromDao() {
        returnSingleDescriptorFromDaoWhenRequested();

        try {
            service.retrieveByExternalId("externalId").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Descriptor 'externalId' must have type COLLECTION, but it is 'SINGLE'"));
        }
    }

    private void returnSingleDescriptorFromDaoWhenRequested() {
        collDescriptorInDb.setType(Descriptor.Type.SINGLE);
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collDescriptorInDb));
    }

    @Test
    void givenDescriptorHasNoCollection_whenRetrievingACollection_thenItShouldBeRetrievedFromDao() {
        collDescriptorInDb.setCollection(null);
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collDescriptorInDb));

        try {
            service.retrieveByExternalId("externalId").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("Descriptor 'externalId' has type COLLECTION, but there is no collection in it"));
        }
    }

    @Test
    void givenNoCollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(reactiveDescriptorDao.store(any())).then(returnFirstArgInMono());

        Descriptor retrievedOrCreated = service.retrieveByCoordinatesOrCreate(owned).block();

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorDao).store(descriptorCaptor.capture());
        Descriptor savedDescriptor = descriptorCaptor.getValue();
        assertThat(retrievedOrCreated, is(sameInstance(savedDescriptor)));

        assertThat(savedDescriptor.getType(), is(Descriptor.Type.COLLECTION));
        assertThat(savedDescriptor.getCollection(), is(sameInstance(owned)));
    }

    @Test
    void givenACollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(reactiveDescriptorDao.store(any()))
                .thenReturn(Mono.error(new DuplicateKeyException("such coordinatesString already exists")));
        when(reactiveDescriptorDao.retrieveByCollectionCoordinates(anyString()))
                .thenReturn(Mono.just(collDescriptorInDb));

        Descriptor retrievedOrCreated = service.retrieveByCoordinatesOrCreate(owned).block();

        assertThat(retrievedOrCreated, is(sameInstance(collDescriptorInDb)));
    }

    @Test
    void givenAnExtractionOverrideExistsSupportingASingleDescriptor_whenRetrievingTheCollectionByTheSingleDescriptorExternalId_thenTheCollectionShouldBeReturnedByTheOverride() {
        returnSingleDescriptorFromDaoWhenRequested();
        service = new ReactiveCollectionDescriptorServiceImpl(reactiveDescriptorDao, descriptorService,
                new ReactiveCollectionOverridesWithDescriptorExtractorList(singletonList(new ExtractionOverride())));

        CollectionDescriptor collectionDescriptor = service.retrieveByExternalId("externalId").block();

        assertThat(collectionDescriptor, is(notNullValue()));
        assertThat(collectionDescriptor.getType(), is(CollectionDescriptor.Type.OWNED));
        OwnedCoordinates ownedCoordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates.getHostId(), is(sameInstance(collDescriptorInDb)));
        assertThat(ownedCoordinates.getHostAttributeName(), is("items"));
    }

    private static class ExtractionOverride implements ReactiveCollectionOverride {
        @Override
        public boolean supports(Descriptor descriptor) {
            return true;
        }

        @Override
        public Mono<Descriptor.Type> typeForGetOperation(Descriptor descriptor) {
            return descriptor.effectiveTypeReactively();
        }

        @Override
        public Mono<CollectionDescriptor> extractCollectionFromDescriptor(Descriptor descriptor) {
            return Mono.just(CollectionDescriptor.forOwned(descriptor, "items"));
        }
    }
}