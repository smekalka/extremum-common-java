package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.exceptions.InvalidDescriptorException;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import reactor.core.publisher.Mono;

public class ReactiveDescriptorIdResolver {

    private final ReactiveDescriptorService descriptorService;

    public ReactiveDescriptorIdResolver(ReactiveDescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    public Mono<Descriptor> resolveIds(Descriptor descriptor) {
        if (descriptor.hasInternalId() && descriptor.hasExternalId()) {
            return Mono.just(descriptor);
        }

        if (descriptor.hasInternalId()) {
            return descriptor.getInternalIdReactively()
                    .flatMap(descriptorService::loadByInternalId)
                    .map(descriptor::copyFieldsFrom)
                    .filter(Descriptor::hasExternalId)
                    .switchIfEmpty(Mono.error(externalIdNotFoundException(descriptor)));
        }

        if (descriptor.hasExternalId()) {
            return descriptor.getExternalIdReactively()
                    .flatMap(descriptorService::loadByExternalId)
                    .map(descriptor::copyFieldsFrom)
                    .filter(Descriptor::hasInternalId)
                    .switchIfEmpty(Mono.error(internalIdNotFoundException(descriptor)));
        }

        return Mono.error(new InvalidDescriptorException("Unable to resolve IDs for descriptor without existing IDs"));
    }

    private DescriptorNotFoundException externalIdNotFoundException(Descriptor descriptor) {
        return new DescriptorNotFoundException(
                "External ID was not found for internal ID " + descriptor.getInternalId(), descriptor);
    }

    private DescriptorNotFoundException internalIdNotFoundException(Descriptor descriptor) {
        return new DescriptorNotFoundException(
                "Internal ID was not found for external ID " + descriptor.getExternalId(), descriptor);
    }

}
