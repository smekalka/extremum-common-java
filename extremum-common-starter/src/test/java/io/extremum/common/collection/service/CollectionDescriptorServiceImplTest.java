package io.extremum.common.collection.service;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static io.extremum.test.mockito.ReturnFirstArg.returnFirstArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionDescriptorServiceImplTest {
    @InjectMocks
    private CollectionDescriptorServiceImpl collectionDescriptorService;

    @Mock
    private DescriptorService descriptorService;
    @Mock
    private DescriptorDao descriptorDao;

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    private final CollectionDescriptor owned = CollectionDescriptor.forOwned(
            new Descriptor("host-id"), "attribute");
    private final Descriptor collDescriptorInDb = Descriptor.forCollection("external-id", owned);

    @Test
    void givenDescriptorContainsCollection_whenRetrievingByExternalId_thenShouldBeRetrievedFromDescriptorService() {
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(collDescriptorInDb));

        CollectionDescriptor collectionDescriptor = collectionDescriptorService.retrieveByExternalId("external-id")
                .orElse(null);

        assertThat(collectionDescriptor, is(notNullValue()));
        assertThat(collectionDescriptor, is(sameInstance(collDescriptorInDb.getCollection())));
    }

    @Test
    void givenDescriptorIsNotACollection_whenRetrievingByExternalId_thenAnExceptionShouldBeThrown() {
        collDescriptorInDb.setType(Descriptor.Type.SINGLE);
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(collDescriptorInDb));

        try {
            collectionDescriptorService.retrieveByExternalId("external-id");
            Assertions.fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Descriptor 'external-id' must have type COLLECTION, but it is 'SINGLE'"));
        }
    }

    @Test
    void givenDescriptorHasNoCollectionDescriptor_whenRetrievingByExternalId_thenAnExceptionShouldBeThrown() {
        collDescriptorInDb.setCollection(null);
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(collDescriptorInDb));

        try {
            collectionDescriptorService.retrieveByExternalId("external-id");
            Assertions.fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("Descriptor 'external-id' has type COLLECTION, but there is no collection in it"));
        }
    }

    @Test
    void givenNoCollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(descriptorDao.store(any())).then(returnFirstArg());

        Descriptor retrievedOrCreated = collectionDescriptorService.retrieveByCoordinatesOrCreate(owned);

        verify(descriptorDao).store(descriptorCaptor.capture());
        Descriptor savedDescriptor = descriptorCaptor.getValue();
        assertThat(retrievedOrCreated, is(sameInstance(savedDescriptor)));

        assertThat(savedDescriptor.getType(), is(Descriptor.Type.COLLECTION));
        assertThat(savedDescriptor.getCollection(), is(sameInstance(owned)));
    }

    @Test
    void givenACollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(descriptorDao.retrieveByCollectionCoordinates(anyString()))
                .thenReturn(Optional.of(collDescriptorInDb));

        Descriptor retrievedOrCreated = collectionDescriptorService.retrieveByCoordinatesOrCreate(owned);

        assertThat(retrievedOrCreated, is(sameInstance(collDescriptorInDb)));
    }
}