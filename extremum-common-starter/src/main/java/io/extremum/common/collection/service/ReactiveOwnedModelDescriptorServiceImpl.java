package io.extremum.common.collection.service;

import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ReactiveOwnedModelDescriptorServiceImpl implements ReactiveOwnedModelDescriptorService {

    public ReactiveOwnedModelDescriptorServiceImpl(ReactiveDescriptorDao reactiveDescriptorDao,
                                                   DescriptorService descriptorService) {
        this.reactiveDescriptorDao = reactiveDescriptorDao;
        this.descriptorSavers = new DescriptorSavers(descriptorService);
    }

    private final ReactiveDescriptorDao reactiveDescriptorDao;
    private final DescriptorSavers descriptorSavers;

    @Override
    public Mono<OwnedModelDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .map(Descriptor::getOwned);
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinates(OwnedModelDescriptor collectionDescriptor) {
        return Mono.defer(() -> retrieveByCoordinates(collectionDescriptor.getCoordinates().getOwnedCoordinates().toCoordinatesString()));
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(OwnedModelDescriptor ownedModelDescriptor) {
        Descriptor descriptor = descriptorSavers.createOwnedModelDescriptor(ownedModelDescriptor);
        return reactiveDescriptorDao.store(descriptor)
                .onErrorResume(DuplicateKeyException.class, e -> retrieveByCoordinates(ownedModelDescriptor));
    }


    private Mono<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return reactiveDescriptorDao.retrieveByOwnedModelCoordinates(coordinatesString)
                .map(descriptor -> descriptor);
    }
}
