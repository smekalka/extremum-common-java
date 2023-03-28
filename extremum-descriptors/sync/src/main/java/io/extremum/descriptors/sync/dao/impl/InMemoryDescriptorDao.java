package io.extremum.descriptors.sync.dao.impl;

import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDescriptorDao implements DescriptorDao {
    private final Map<String, Descriptor> descriptorMap = new ConcurrentHashMap<>();

    @Override
    public Optional<Descriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptorMap.get(externalId));
    }

    @Override
    public Optional<Descriptor> retrieveByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> retrieveMapByExternalIds(Collection<String> externalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Descriptor> retrieveDescriptors(Collection<String> externalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        descriptorMap.put(descriptor.getExternalId(), descriptor);
        return descriptor;
    }

    @Override
    public List<Descriptor> storeBatch(List<Descriptor> descriptors) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroyBatch(List<Descriptor> descriptors) {
        throw new UnsupportedOperationException();
    }
}
