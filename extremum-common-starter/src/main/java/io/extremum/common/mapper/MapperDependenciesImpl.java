package io.extremum.common.mapper;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import lombok.RequiredArgsConstructor;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class MapperDependenciesImpl implements MapperDependencies {
    private final DescriptorFactory descriptorFactory;

    @Override
    public DescriptorFactory descriptorFactory() {
        return descriptorFactory;
    }
}
