package io.extremum.descriptors.sync.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DescriptorDao {

    Optional<Descriptor> retrieveByExternalId(String externalId);

    Optional<Descriptor> retrieveByInternalId(String internalId);

    Optional<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates);

    Map<String, String> retrieveMapByExternalIds(Collection<String> externalIds);

    Map<String, Descriptor> retrieveDescriptors(Collection<String> externalIds);

    Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds);

    Descriptor store(Descriptor descriptor);

    List<Descriptor> storeBatch(List<Descriptor> descriptors);

    void destroyBatch(List<Descriptor> descriptors);
}
