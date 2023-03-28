package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.extremum.sharedmodels.structs.IdListOrObjectList;

import java.io.IOException;

public class IdListOrObjectListSerializer extends StdSerializer<IdListOrObjectList> {
    public IdListOrObjectListSerializer() {
        super(IdListOrObjectList.class);
    }

    @Override
    public void serialize(IdListOrObjectList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || !value.isContainsIdList()) {
            gen.writeNull();
        } else {
            gen.writeStartArray();

            for (Object id : value.idList) {
                gen.writeString(id.toString());
            }

            gen.writeEndArray();
        }
    }
}
