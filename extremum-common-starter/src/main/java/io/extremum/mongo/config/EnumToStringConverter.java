package io.extremum.mongo.config;

import io.extremum.common.annotation.ToStorageString;
import io.extremum.common.utils.InstanceMethods;
import io.extremum.common.utils.ReflectionUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.lang.reflect.Method;

/**
 * @author rpuch
 */
@WritingConverter
public class EnumToStringConverter implements Converter<Enum<?>, String>, ConditionalConverter {
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Class<?> enumType = sourceType.getType();
        return new InstanceMethods(enumType).stream()
                .anyMatch(this::isToStorageMethod);
    }

    private boolean isToStorageMethod(Method method) {
        return method.isAnnotationPresent(ToStorageString.class)
                && method.getReturnType() == String.class
                && method.getParameterCount() == 0;
    }

    @Override
    public String convert(Enum<?> source) {
        Method toStorageMethod = new InstanceMethods(source.getClass()).stream()
                .filter(this::isToStorageMethod)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        "Did not find a 'to-storage' method on " + source.getClass()));

        return (String) ReflectionUtils.invokeMethod(toStorageMethod, source);
    }
}
