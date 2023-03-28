package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.basic.IntegerOrString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class IntegerOrStringSerializer extends StdSerializer<IntegerOrString> {
    public IntegerOrStringSerializer() {
        super(IntegerOrString.class);
    }

    @Override
    public void serialize(IntegerOrString value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            if (value.isInteger()) {
                gen.writeNumber(value.getIntegerValue());
            } else if (value.isString()) {
                gen.writeString(value.getStringValue());
            } else {
                log.error("Unknown type of IntegerOrString object: {}", value.getType());
                gen.writeNull();
            }
        }
    }

    @Override
    public void serializeWithType(IntegerOrString value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        serialize(value, gen, serializers);
    }
}
