package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorCoordinatesMapLoader extends CarefulMapLoader<String, String> {
    private final DescriptorRepository repository;

    public DescriptorCoordinatesMapLoader(DescriptorRepository repository) {
        this.repository = repository;
    }

    @Override
    public String load(String key) {
        return repository.findByCollectionCoordinatesString(key)
                .map(Descriptor::getExternalId)
                .orElse(null);
    }
}
