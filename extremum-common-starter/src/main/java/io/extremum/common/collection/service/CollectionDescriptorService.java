package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface CollectionDescriptorService {
    Optional<CollectionDescriptor> retrieveByExternalId(String externalId);

    Descriptor retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor);
}
