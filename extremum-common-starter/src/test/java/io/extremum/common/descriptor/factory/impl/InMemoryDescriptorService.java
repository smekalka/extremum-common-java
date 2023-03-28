package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.utils.ReflectionUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class InMemoryDescriptorService implements DescriptorService {
    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    private final ConcurrentMap<String, Descriptor> externalIdToDescriptorMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Descriptor> internalIdToDescriptorMap = new ConcurrentHashMap<>();

    @Override
    public String createExternalId() {
        return uuidGenerator.generateUUID();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        String externalId = ReflectionUtils.getFieldValue(descriptor, "externalId");
        if (externalId == null) {
            externalId = createExternalId();
            ReflectionUtils.setFieldValue(descriptor, "externalId", externalId);
        }

        externalIdToDescriptorMap.put(externalId, descriptor);

        String internalId = ReflectionUtils.getFieldValue(descriptor, "internalId");
        if (internalId != null) {
            internalIdToDescriptorMap.put(internalId, descriptor);
        }

        return descriptor;
    }

    @Override
    public List<Descriptor> storeBatch(List<Descriptor> descriptors) {
        descriptors.forEach(this::store);
        return descriptors;
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String externalId) {
        return Optional.ofNullable(externalIdToDescriptorMap.get(externalId));
    }

    @Override
    public Optional<Descriptor> loadByInternalId(String internalId) {
        return Optional.ofNullable(internalIdToDescriptorMap.get(internalId));
    }

    @Override
    public Map<String, String> loadMapByExternalIds(Collection<String> externalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Descriptor> loadDescriptorsByExternalIds(Collection<String> externalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> loadMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Descriptor makeDescriptorReady(String descriptorExternalId, String modelType) {
        throw new UnsupportedOperationException();
    }

    public Stream<Descriptor> descriptors() {
        return externalIdToDescriptorMap.values().stream();
    }
}
