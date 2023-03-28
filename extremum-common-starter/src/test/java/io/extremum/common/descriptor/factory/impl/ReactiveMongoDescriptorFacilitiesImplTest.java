package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.collection.conversion.InMemoryReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.InMemoryReactiveOwnedModelDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilitiesImpl;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveMongoDescriptorFacilitiesImplTest {
    private ReactiveMongoDescriptorFacilitiesImpl facilities;

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private InMemoryReactiveDescriptorService reactiveDescriptorService = new InMemoryReactiveDescriptorService();
    @Spy
    private ReactiveCollectionDescriptorService reactiveCollectionDescriptorService =
            new InMemoryReactiveCollectionDescriptorService(reactiveDescriptorService, descriptorService);
    @Spy
    private ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService =
            new InMemoryReactiveOwnedModelDescriptorService();


    private final ObjectId objectId = new ObjectId();

    @BeforeEach
    void initDescriptorSaver() {
        ReactiveDescriptorSaver descriptorSaver = new ReactiveDescriptorSaver(
                descriptorService, reactiveDescriptorService, reactiveCollectionDescriptorService,reactiveOwnedModelDescriptorService );
        ReactiveDescriptorIdResolver descriptorIdResolver = new ReactiveDescriptorIdResolver(reactiveDescriptorService);
        facilities = new ReactiveMongoDescriptorFacilitiesImpl(new DescriptorFactory(), descriptorSaver,
                descriptorIdResolver);
    }

    @Test
    void whenCreatingOrSavingADescriptorWithANewInternalId_thenARandomObjectIdShouldBeGeneratedAndDescriptorSavedWithThatId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        Descriptor descriptor = facilities.createOrGet(objectId.toString(), "Test", "iri").block();

        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is("external-id"));
        assertThat(descriptor.getInternalId(), is(equalTo(objectId.toString())));
        assertThat(descriptor.getModelType(), is("Test"));
        assertThat(descriptor.getStorageType(), is("mongo"));

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorService).store(descriptor);
    }

    @Test
    void whenCreatingOrSavingADescriptorWithAnExistingInternalId_thenTheExistingDescriptorShouldBeReturned() {
        doReturn(Mono.error(new DuplicateKeyException("dup")))
                .when(reactiveDescriptorService).store(any());
        doReturn(Mono.just(Descriptor.builder().externalId("external-id").build()))
                .when(reactiveDescriptorService).loadByInternalId(objectId.toString());

        Descriptor descriptor = facilities.createOrGet(objectId.toString(), "Test", "iri").block();

        assertThat(descriptor.getExternalId(), is("external-id"));
    }

    @Test
    void whenCreatingADescriptorFromInternalId_thenItShouldBeFilledWithInternalId() {
        doReturn(Mono.just(Descriptor.builder().externalId("external-id").internalId(objectId.toString())
                .storageType(StandardStorageType.MONGO).build()))
                .when(reactiveDescriptorService).loadByInternalId(objectId.toString());

        Descriptor descriptor = facilities.fromInternalId(objectId.toString()).block();

        assertThat(descriptor.getInternalId(), is(equalTo(objectId.toString())));
        assertThat(descriptor.getStorageType(), is("mongo"));
    }

    @Test
    void givenDescriptorIsForMongo_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(objectId.toString())
                .storageType(StandardStorageType.MONGO)
                .build();

        String resolvedId = facilities.resolve(descriptor).block();

        assertThat(resolvedId, is(equalTo(objectId.toString())));
    }

    @Test
    void givenDescriptorIsNotForMongo_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(objectId.toString())
                .storageType(StandardStorageType.POSTGRES)
                .build();

        Mono<String> mono = facilities.resolve(descriptor);
        try {
            mono.block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Wrong descriptor storage type postgres"));
        }
    }
}