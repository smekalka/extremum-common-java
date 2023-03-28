package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.basic.NumberOrObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class NumberOrObjectSerializer extends StdSerializer<NumberOrObject<? extends Number, ?>> {
    public NumberOrObjectSerializer() {
        super(NumberOrObject.class, true);
    }

    @Override
    public void serialize(NumberOrObject<? extends Number, ?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value.isNumber()) {
            gen.writeString(String.valueOf(value.getNumber()));
        } else {
            JsonSerializer<Object> ser = provider.findTypedValueSerializer(value.getObject().getClass(), false, null);
            ser.serialize(value.getObject(), gen, provider);
        }
    }
}
