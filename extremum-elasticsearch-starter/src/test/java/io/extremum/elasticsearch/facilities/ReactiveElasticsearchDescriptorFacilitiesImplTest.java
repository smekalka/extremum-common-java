package io.extremum.elasticsearch.facilities;

import io.extremum.common.collection.service.InMemoryReactiveOwnedModelDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.test.core.descriptor.InMemoryDescriptorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveElasticsearchDescriptorFacilitiesImplTest {
    private ReactiveElasticsearchDescriptorFacilitiesImpl facilities;

    @Spy
    private final DescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private final InMemoryReactiveDescriptorService reactiveDescriptorService = new InMemoryReactiveDescriptorService();
    @Spy
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService =
            new InMemoryReactiveCollectionDescriptorService(reactiveDescriptorService, descriptorService);

    @Spy
    private final ReactiveOwnedModelDescriptorService ownedModelDescriptorService =
            new InMemoryReactiveOwnedModelDescriptorService();

    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void initDescriptorSaver() {
        ReactiveDescriptorIdResolver descriptorIdResolver = new ReactiveDescriptorIdResolver(reactiveDescriptorService);

        ReactiveDescriptorSaver descriptorSaver = new ReactiveDescriptorSaver(
                descriptorService, reactiveDescriptorService, reactiveCollectionDescriptorService, ownedModelDescriptorService);
        facilities = new ReactiveElasticsearchDescriptorFacilitiesImpl(new DescriptorFactory(), descriptorSaver, descriptorIdResolver);
    }

    @Test
    void whenCreatingANewDescriptorWithANewInternalId_thenARandomObjectIdShouldBeGeneratedAndDescriptorSavedWithThatId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        Descriptor descriptor = facilities.createOrGet(uuid.toString(), "Test", "iri").block();

        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is("external-id"));
        assertThat(descriptor.getInternalId(), is(equalTo(uuid.toString())));
        assertThat(descriptor.getModelType(), is("Test"));
        assertThat(descriptor.getStorageType(), is("elastic"));

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorService).store(descriptor);
    }

    @Test
    void givenDescriptorIsForElasticsearch_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(uuid.toString())
                .storageType(StandardStorageType.ELASTICSEARCH)
                .build();

        String resolvedId = facilities.resolve(descriptor).block();

        assertThat(resolvedId, is(equalTo(uuid.toString())));
    }

    @Test
    void givenDescriptorIsNotForElasticsearch_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(uuid.toString())
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