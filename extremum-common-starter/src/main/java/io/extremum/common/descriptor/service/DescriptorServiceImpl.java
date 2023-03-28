package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public final class DescriptorServiceImpl implements DescriptorService {
    private final DescriptorDao descriptorDao;
    private final UUIDGenerator uuidGenerator;

    private final DescriptorReadinessValidation descriptorReadinessValidation = new DescriptorReadinessValidation();

    @Override
    public String createExternalId() {
        return uuidGenerator.generateUUID();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        Objects.requireNonNull(descriptor, "Descriptor is null");
        return descriptorDao.store(descriptor);
    }

    @Override
    public List<Descriptor> storeBatch(List<Descriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors list is null");
        return descriptorDao.storeBatch(descriptors);
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String externalId) {
        Objects.requireNonNull(externalId, "externalId is null");

        return descriptorDao.retrieveByExternalId(externalId)
                .map(descriptor -> {
                    if (descriptor.getReadiness() == Descriptor.Readiness.BLANK) {
                        throw new DescriptorNotReadyException(
                                String.format("Descriptor with external ID '%s' is not ready yet", externalId));
                    }
                    return descriptor;
                });
    }

    @Override
    public Optional<Descriptor> loadByInternalId(String internalId) {
        Objects.requireNonNull(internalId, "internalId is null");
        return descriptorDao.retrieveByInternalId(internalId);
    }

    @Override
    public Map<String, String> loadMapByExternalIds(Collection<String> externalIds) {
        Objects.requireNonNull(externalIds, "List of external ids can't be null");
        return descriptorDao.retrieveMapByExternalIds(externalIds);
    }

    @Override
    public Map<String, Descriptor> loadDescriptorsByExternalIds(Collection<String> externalIds) {
        Objects.requireNonNull(externalIds, "List of external ids can't be null");
        return descriptorDao.retrieveDescriptors(new HashSet<>(externalIds));
    }

    @Override
    public Map<String, String> loadMapByInternalIds(Collection<String> internalIds) {
        Objects.requireNonNull(internalIds, "List of internal ids can't be null");
        return descriptorDao.retrieveMapByInternalIds(internalIds);
    }

    @Override
    public Descriptor makeDescriptorReady(String descriptorExternalId, String modelType) {
        Descriptor descriptor = descriptorDao.retrieveByExternalId(descriptorExternalId)
                .orElseThrow(() -> doesNotExistException(descriptorExternalId));

        descriptorReadinessValidation.validateDescriptorIsNotReady(descriptorExternalId, descriptor);

        descriptor.setReadiness(Descriptor.Readiness.READY);
        descriptor.setModelType(modelType);

        return descriptorDao.store(descriptor);
    }

    private DescriptorNotFoundException doesNotExistException(String descriptorExternalId) {
        String errorMessage = String.format("No descriptor with externalId '%s' exists", descriptorExternalId);
        return new DescriptorNotFoundException(errorMessage, Descriptor.builder().externalId(descriptorExternalId).build());
    }
}
