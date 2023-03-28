package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.structs.IntegerRangeOrValue;

import java.io.IOException;

public class IntegerRangeOrValueSerializer extends StdSerializer<IntegerRangeOrValue> {
    public IntegerRangeOrValueSerializer() {
        super(IntegerRangeOrValue.class);
    }

    @Override
    public void serialize(IntegerRangeOrValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.isInteger()) {
            gen.writeNumber(value.value);
        } else {
            gen.writeStartObject();

            if (value.min == null) {
                gen.writeNullField(IntegerRangeOrValue.FIELDS.min.name());
            } else {
                gen.writeNumberField(IntegerRangeOrValue.FIELDS.min.name(), value.min);
            }

            if (value.max == null) {
                gen.writeNullField(IntegerRangeOrValue.FIELDS.max.name());
            } else {
                gen.writeNumberField(IntegerRangeOrValue.FIELDS.max.name(), value.max);
            }

            gen.writeEndObject();
        }
    }
}
