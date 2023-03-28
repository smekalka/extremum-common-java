package io.extremum.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.common.facilities.ReactiveDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class ReactiveDescriptorFacilitiesImpl implements ReactiveDescriptorFacilities {

    private final DescriptorFactory descriptorFactory;
    private final ReactiveDescriptorSaver descriptorSaver;
    private final ReactiveDescriptorIdResolver descriptorIdResolver;

    @Override
    public Mono<Descriptor> createOrGet(String internalId, String modelType, String iri) {
        return descriptorSaver.createAndSave(internalId, modelType, storageType(), iri);
    }

    @Override
    public Mono<Descriptor> fromInternalId(String internalId) {
        return Mono.just(descriptorFactory.fromInternalId(internalId, storageType()))
                .flatMap(descriptorIdResolver::resolveIds);
    }

    @Override
    public Mono<String> resolve(Descriptor descriptor) {
        return DescriptorResolver.resolveReactively(descriptor, storageType());
    }

    protected abstract StorageType storageType();
}
