package io.extremum.mapper.jackson.modifier;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import io.extremum.sharedmodels.basic.StringOrObject;

import java.lang.reflect.Type;

public class StringOrObjectTypeModifier extends TypeModifier {
    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
        Class<?> raw = type.getRawClass();
        if (StringOrObject.class.isAssignableFrom(raw))
            return ReferenceType.upgradeFrom(type, type.containedTypeOrUnknown(0));
        return type;
    }
}
