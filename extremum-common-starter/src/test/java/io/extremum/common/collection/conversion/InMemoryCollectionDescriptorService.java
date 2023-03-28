package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public class InMemoryCollectionDescriptorService implements CollectionDescriptorService {
    private final InMemoryDescriptorService descriptorService;

    public InMemoryCollectionDescriptorService(InMemoryDescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return descriptorService.loadByExternalId(externalId).map(Descriptor::getCollection);
    }

    @Override
    public Descriptor retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        return retrieveByCoordinates(collectionDescriptor.toCoordinatesString())
                .orElseGet(() -> {
                    Descriptor descriptor = new DescriptorSavers(descriptorService).createCollectionDescriptor(
                            collectionDescriptor);
                    return descriptorService.store(descriptor);
                });
    }

    private Optional<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return descriptorService.descriptors()
                .filter(descriptor -> descriptor.effectiveType() == Descriptor.Type.COLLECTION)
                .filter(descriptor -> descriptor.getCollection().toCoordinatesString().equals(coordinatesString))
                .findAny();
    }
}
