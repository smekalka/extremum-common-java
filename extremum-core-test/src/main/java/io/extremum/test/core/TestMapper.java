package io.extremum.test.core;

import io.extremum.common.mapper.SystemJsonObjectMapper;

public class TestMapper extends SystemJsonObjectMapper {
    public TestMapper() {
        super(new MockedMapperDependencies());
    }
}
