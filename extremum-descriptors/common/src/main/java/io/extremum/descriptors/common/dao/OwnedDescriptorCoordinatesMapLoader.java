package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

public class OwnedDescriptorCoordinatesMapLoader extends CarefulMapLoader<String, String> {
    private final DescriptorRepository repository;

    public OwnedDescriptorCoordinatesMapLoader(DescriptorRepository repository) {
        this.repository = repository;
    }

    @Override
    public String load(String key) {
        return repository.findByOwnedModelCoordinatesString(key)
                .map(Descriptor::getExternalId)
                .orElse(null);
    }
}
