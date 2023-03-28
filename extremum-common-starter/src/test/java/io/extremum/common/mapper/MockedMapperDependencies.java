package io.extremum.common.mapper;

import io.extremum.common.descriptor.factory.DescriptorFactory;

/**
 * @author rpuch
 */
public class MockedMapperDependencies implements MapperDependencies {
    private final DescriptorFactory descriptorFactory = new DescriptorFactory();

    @Override
    public DescriptorFactory descriptorFactory() {
        return descriptorFactory;
    }
}
