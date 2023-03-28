package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import org.apache.commons.lang3.NotImplementedException;
import reactor.core.publisher.Mono;

public class InMemoryReactiveOwnedModelDescriptorService implements ReactiveOwnedModelDescriptorService {
    @Override
    public Mono<OwnedModelDescriptor> retrieveByExternalId(String externalId) {
        throw new NotImplementedException();
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinates(OwnedModelDescriptor collectionDescriptor) {
        throw new NotImplementedException();
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(OwnedModelDescriptor collectionDescriptor) {
        throw new NotImplementedException();
    }
}
