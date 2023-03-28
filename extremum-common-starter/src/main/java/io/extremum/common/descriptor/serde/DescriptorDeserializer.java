package io.extremum.common.descriptor.serde;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class DescriptorDeserializer extends StdScalarDeserializer<Descriptor> {
    private final DescriptorFactory descriptorFactory;

    public DescriptorDeserializer(DescriptorFactory descriptorFactory) {
        super(Descriptor.class);

        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public Descriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String externalId = _parseString(p, ctxt);
        return descriptorFactory.fromExternalId(externalId);
    }
}
