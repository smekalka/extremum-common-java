package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.structs.DurationVariativeValue;

import java.io.IOException;

public class DurationVariativeValueSerializer extends StdSerializer<DurationVariativeValue> {
    public DurationVariativeValueSerializer() {
        super(DurationVariativeValue.class);
    }

    @Override
    public void serialize(DurationVariativeValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value.isContainsCommonValueString()) {
            gen.writeString(value.commonStringValue);
        } else if (value.isContainsCommonValueInteger()) {
            gen.writeNumber(value.commonIntValue);
        } else if (value.isContainsObject()) {
            gen.writeStartObject();

            if (value.isContainsMinValueInteger()) {
                gen.writeNumberField(DurationVariativeValue.FIELDS.min.name(), value.minIntValue);
            }

            if (value.isContainsMinValueString()) {
                gen.writeStringField(DurationVariativeValue.FIELDS.min.name(), value.minStringValue);
            }

            if (value.isContainsMaxValueInteger()) {
                gen.writeNumberField(DurationVariativeValue.FIELDS.max.name(), value.maxIntValue);
            }

            if (value.isContainsMaxValueString()) {
                gen.writeStringField(DurationVariativeValue.FIELDS.max.name(), value.maxStringValue);
            }

            gen.writeEndObject();
        } else {
            gen.writeNull();
        }
    }
}