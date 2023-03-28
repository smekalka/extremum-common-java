package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;

import java.util.Map;
import java.util.UUID;

public abstract class UUIDDescriptorFacilities {
    private final DescriptorFactory descriptorFactory;
    private final DescriptorSaver descriptorSaver;
    private final DescriptorIdResolver descriptorIdResolver;

    protected UUIDDescriptorFacilities(DescriptorFactory descriptorFactory, DescriptorSaver descriptorSaver,
            DescriptorIdResolver descriptorIdResolver) {
        this.descriptorFactory = descriptorFactory;
        this.descriptorSaver = descriptorSaver;
        this.descriptorIdResolver = descriptorIdResolver;
    }

    protected abstract StorageType storageType();

    public Descriptor create(UUID uuid, String modelType, String iri, Map<String, Object> stump) {
        return descriptorSaver.createAndSave(uuid.toString(), modelType, storageType(), iri, stump);
    }

    public Descriptor save(Descriptor descriptor) {
        return descriptorSaver.save(descriptor);
    }

    public Descriptor fromInternalId(UUID uuid) {
        return fromInternalId(uuid.toString());
    }

    public Descriptor fromInternalId(String uuid) {
        Descriptor descriptor = descriptorFactory.fromInternalId(uuid, storageType());
        return descriptorIdResolver.resolveIds(descriptor);
    }

    public Descriptor fromInternalIdOrNull(String uuid) {
        Descriptor descriptor = descriptorFactory.fromInternalIdOrNull(uuid, storageType());
        if (descriptor == null) {
            return null;
        }
        return descriptorIdResolver.resolveIds(descriptor);
    }

    public UUID resolve(Descriptor descriptor) {
        String internalId = DescriptorResolver.resolve(descriptor, storageType());
        return UUID.fromString(internalId);
    }
}
