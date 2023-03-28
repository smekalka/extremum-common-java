package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorInternalIdMapLoader extends CarefulMapLoader<String, String> {
    private final DescriptorRepository descriptorRepository;

    public DescriptorInternalIdMapLoader(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public String load(String key) {
        return descriptorRepository.findByInternalId(key)
                .map(Descriptor::getExternalId)
                .orElse(null);
    }
}
