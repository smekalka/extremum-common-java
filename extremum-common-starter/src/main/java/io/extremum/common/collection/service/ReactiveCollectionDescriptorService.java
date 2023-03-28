package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveCollectionDescriptorService {
    Mono<CollectionDescriptor> retrieveByExternalId(String externalId);

    Mono<Descriptor> retrieveByCoordinates(CollectionDescriptor collectionDescriptor);

    Mono<Descriptor> retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor);
}
