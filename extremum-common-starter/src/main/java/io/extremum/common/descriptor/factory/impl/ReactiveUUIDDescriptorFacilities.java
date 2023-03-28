package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class ReactiveUUIDDescriptorFacilities {
    private final ReactiveDescriptorSaver descriptorSaver;

    protected ReactiveUUIDDescriptorFacilities(ReactiveDescriptorSaver descriptorSaver) {
        this.descriptorSaver = descriptorSaver;
    }

    protected abstract StorageType storageType();

    public Mono<Descriptor> create(UUID uuid, String modelType, String iri) {
        return descriptorSaver.createAndSave(uuid.toString(), modelType, storageType(), iri);
    }

    public Mono<String> resolve(Descriptor descriptor) {
        return DescriptorResolver.resolveReactively(descriptor, storageType());
    }
}
