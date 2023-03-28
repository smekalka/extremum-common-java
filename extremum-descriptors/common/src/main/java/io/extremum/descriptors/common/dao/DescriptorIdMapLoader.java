package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorIdMapLoader extends CarefulMapLoader<String, Descriptor> {
    private final DescriptorRepository descriptorRepository;

    public DescriptorIdMapLoader(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public Descriptor load(String key) {
        return descriptorRepository.findByExternalId(key).orElse(null);
    }
}
