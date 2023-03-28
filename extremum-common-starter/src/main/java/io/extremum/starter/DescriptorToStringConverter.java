package io.extremum.starter;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

/**
 * @author rpuch
 */
@WritingConverter
public class DescriptorToStringConverter implements Converter<Descriptor, String> {
    @Override
    public String convert(Descriptor source) {
        if (source == null) {
            return null;
        }
        return source.getExternalId();
    }
}
