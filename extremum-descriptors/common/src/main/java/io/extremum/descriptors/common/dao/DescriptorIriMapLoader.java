package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorIriMapLoader extends CarefulMapLoader<String, String>{

    private final DescriptorRepository descriptorRepository;

    public DescriptorIriMapLoader(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public String load(String key) {
        return descriptorRepository.findByIri(key)
                .map(Descriptor::getExternalId)
                .orElse(null);
    }
}