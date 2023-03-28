package io.extremum.common.descriptor.serde;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorSerializer extends ToStringSerializerBase {
    public DescriptorSerializer() {
        super(Descriptor.class);
    }

    @Override
    public String valueToString(Object value) {
        if (!(value instanceof Descriptor)) {
            throw new IllegalStateException("I can only serialize Descriptor instances");
        }
        Descriptor descriptor = (Descriptor) value;
        return descriptor.getExternalId();
    }
}
