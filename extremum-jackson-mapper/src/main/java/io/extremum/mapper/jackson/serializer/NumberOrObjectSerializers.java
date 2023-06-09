package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import io.extremum.sharedmodels.basic.NumberOrObject;

public class NumberOrObjectSerializers extends Serializers.Base {
    @Override
    public JsonSerializer<?> findReferenceSerializer(SerializationConfig config,
                                                     ReferenceType type, BeanDescription beanDesc,
                                                     TypeSerializer contentTypeSerializer,
                                                     JsonSerializer<Object> contentValueSerializer) {
        Class<?> raw = type.getRawClass();
        if (raw == NumberOrObject.class)
            return new NumberOrObjectSerializer();
        return super.findReferenceSerializer(config, type, beanDesc, contentTypeSerializer, contentValueSerializer);
    }

}
