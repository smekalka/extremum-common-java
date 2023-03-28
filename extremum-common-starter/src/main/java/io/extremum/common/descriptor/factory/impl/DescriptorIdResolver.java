package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.exceptions.InvalidDescriptorException;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;

public class DescriptorIdResolver {

    private final DescriptorService descriptorService;

    public DescriptorIdResolver(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    public Descriptor resolveIds(Descriptor descriptor) {
        if (descriptor.hasInternalId() && descriptor.hasExternalId()) {
            return descriptor;
        }

        if (descriptor.hasInternalId()) {
            return descriptorService.loadByInternalId(descriptor.getInternalId())
                    .map(descriptor::copyFieldsFrom)
                    .filter(Descriptor::hasExternalId)
                    .orElseThrow(() -> externalIdNotFoundException(descriptor));
        }

        if (descriptor.hasExternalId()) {
            return descriptorService.loadByExternalId(descriptor.getExternalId())
                    .map(descriptor::copyFieldsFrom)
                    .filter(Descriptor::hasInternalId)
                    .orElseThrow(() -> internalIdNotFoundException(descriptor));
        }

        throw new InvalidDescriptorException("Unable to resolve IDs for descriptor without existing IDs");
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
