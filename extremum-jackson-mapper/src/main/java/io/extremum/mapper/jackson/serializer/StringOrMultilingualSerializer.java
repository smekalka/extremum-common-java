package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;

import java.io.IOException;
import java.util.Map;

public class StringOrMultilingualSerializer extends StdSerializer<StringOrMultilingual> {
    public StringOrMultilingualSerializer() {
        super(StringOrMultilingual.class);
    }

    @Override
    public void serialize(StringOrMultilingual value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.getType() == StringOrMultilingual.Type.TEXT) {
            gen.writeString(value.getText());
        } else if (value.getType() == StringOrMultilingual.Type.MAP) {
            gen.writeStartObject();

            for (Map.Entry<MultilingualLanguage, String> entry : value.getMultilingualContent().getMap().entrySet()) {
                gen.writeStringField(entry.getKey().getValue(), entry.getValue());
            }

            gen.writeEndObject();
        } else {
            gen.writeNull();
        }
    }

    @Override
    public void serializeWithType(StringOrMultilingual value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        serialize(value, gen, serializers);
    }
}
