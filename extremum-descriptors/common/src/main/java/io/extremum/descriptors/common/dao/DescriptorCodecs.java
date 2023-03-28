package io.extremum.descriptors.common.dao;

import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Constants;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

import java.util.Locale;

public class DescriptorCodecs {
    public static Codec codecForDescriptor() {
        return new TypedJsonJacksonCodec(String.class, Descriptor.class,
                new BasicJsonObjectMapper(Locale.forLanguageTag(Constants.DEFAULT_LOCALE)));
    }

    private DescriptorCodecs() {
    }
}
