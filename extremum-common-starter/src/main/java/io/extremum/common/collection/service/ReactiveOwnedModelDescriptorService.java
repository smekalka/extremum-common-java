package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import reactor.core.publisher.Mono;

public interface ReactiveOwnedModelDescriptorService {

    Mono<OwnedModelDescriptor> retrieveByExternalId(String externalId);

    Mono<Descriptor> retrieveByCoordinates(OwnedModelDescriptor collectionDescriptor);

    Mono<Descriptor> retrieveByCoordinatesOrCreate(OwnedModelDescriptor collectionDescriptor);
}
