package io.extremum.common.descriptor.serde;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class StringToDescriptorConverter implements Converter<String, Descriptor> {
    private final DescriptorFactory descriptorFactory;

    @Override
    public Descriptor convert(String stringValue) {
        return descriptorFactory.fromExternalId(stringValue);
    }
}
