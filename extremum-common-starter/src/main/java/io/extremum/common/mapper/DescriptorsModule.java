package io.extremum.common.mapper;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.extremum.common.descriptor.serde.DescriptorDeserializer;
import io.extremum.common.descriptor.serde.DescriptorSerializer;
import io.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class DescriptorsModule extends SimpleModule {
    public DescriptorsModule(MapperDependencies dependencies) {
        addSerializer(Descriptor.class, new DescriptorSerializer());
        addDeserializer(Descriptor.class, new DescriptorDeserializer(dependencies.descriptorFactory()));
    }
}
