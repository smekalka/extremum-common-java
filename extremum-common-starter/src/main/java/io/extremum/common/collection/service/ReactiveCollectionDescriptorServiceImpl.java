package io.extremum.common.collection.service;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

public class ReactiveCollectionDescriptorServiceImpl implements ReactiveCollectionDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;
    private final DescriptorSavers descriptorSavers;
    private final ReactiveCollectionDescriptorExtractor collectionExtractor;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public ReactiveCollectionDescriptorServiceImpl(ReactiveDescriptorDao reactiveDescriptorDao,
            DescriptorService descriptorService, ReactiveCollectionDescriptorExtractor collectionExtractor) {
        this.reactiveDescriptorDao = reactiveDescriptorDao;
        this.descriptorSavers = new DescriptorSavers(descriptorService);
        this.collectionExtractor = collectionExtractor;
    }

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .flatMap(collectionExtractor::extractCollectionFromDescriptor);
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinates(CollectionDescriptor collectionDescriptor) {
        return Mono.defer(() -> retrieveByCoordinates(collectionDescriptor.toCoordinatesString()));
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = descriptorSavers.createCollectionDescriptor(collectionDescriptor);
        return reactiveDescriptorDao.store(descriptor)
                .onErrorResume(DuplicateKeyException.class, e -> retrieveByCoordinates(collectionDescriptor));
    }

    private Mono<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return reactiveDescriptorDao.retrieveByCollectionCoordinates(coordinatesString)
                .map(descriptor -> {
                    collectionDescriptorVerifier.makeSureDescriptorContainsCollection(
                            descriptor.getExternalId(), descriptor);
                    return descriptor;
                });
    }
}
