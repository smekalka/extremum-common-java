package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.factory.impl.InMemoryReactiveDescriptorService;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class InMemoryReactiveCollectionDescriptorService implements ReactiveCollectionDescriptorService {
    private final InMemoryReactiveDescriptorService reactiveDescriptorService;
    private final DescriptorService descriptorService;

    public InMemoryReactiveCollectionDescriptorService(InMemoryReactiveDescriptorService reactiveDescriptorService,
                                                       DescriptorService descriptorService) {
        this.reactiveDescriptorService = reactiveDescriptorService;
        this.descriptorService = descriptorService;
    }

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorService.loadByExternalId(externalId).map(Descriptor::getCollection);
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinates(CollectionDescriptor collectionDescriptor) {
        return Mono.defer(() -> retrieveByCoordinates(collectionDescriptor.toCoordinatesString()));
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        return retrieveByCoordinates(collectionDescriptor)
                .switchIfEmpty(Mono.defer(() -> storeCollectionDescriptor(collectionDescriptor)));
    }

    private Mono<Descriptor> retrieveByCoordinates(String coordinatesString) {
        Descriptor descriptorOrNull = reactiveDescriptorService.descriptors()
                .filter(descriptor -> descriptor.effectiveType() == Descriptor.Type.COLLECTION)
                .filter(descriptor -> descriptor.getCollection().toCoordinatesString().equals(coordinatesString))
                .findAny()
                .orElse(null);
        return Mono.justOrEmpty(descriptorOrNull);
    }

    private Mono<? extends Descriptor> storeCollectionDescriptor(CollectionDescriptor collectionDescriptor) {
        DescriptorSavers descriptorSavers = new DescriptorSavers(descriptorService);
        Descriptor descriptor = descriptorSavers.createCollectionDescriptor(collectionDescriptor);
        return reactiveDescriptorService.store(descriptor);
    }
}
