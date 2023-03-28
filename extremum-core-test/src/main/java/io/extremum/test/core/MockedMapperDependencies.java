package io.extremum.test.core;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.MapperDependencies;

/**
 * @author rpuch
 */
public class MockedMapperDependencies implements MapperDependencies {
    @Override
    public DescriptorFactory descriptorFactory() {
        return new DescriptorFactory();
    }
}
