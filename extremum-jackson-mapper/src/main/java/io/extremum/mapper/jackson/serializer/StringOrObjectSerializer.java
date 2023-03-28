package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.basic.StringOrObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class StringOrObjectSerializer extends StdSerializer<StringOrObject<?>> {
    StringOrObjectSerializer() {
        super(StringOrObject.class, true);
    }

    @Override
    public void serialize(StringOrObject<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value.isComplex() && value.getObject() == null)
            gen.writeNull();
        else if (value.isComplex()) {
            JsonSerializer<Object> ser = provider.findTypedValueSerializer(value.getObject().getClass(), false, null);
            ser.serialize(value.getObject(), gen, provider);
        }
        else if (value.isSimple()) {
            gen.writeString(value.getString());
        }
        else {
            log.debug("Nothing to serialize, unknown value type");
            gen.writeNull();
        }
    }
}
