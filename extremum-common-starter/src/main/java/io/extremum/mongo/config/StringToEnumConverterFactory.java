package io.extremum.mongo.config;

import io.extremum.common.annotation.FromStorageString;
import io.extremum.common.utils.ClassMethods;
import io.extremum.common.utils.ReflectionUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

import java.lang.reflect.Method;

/**
 * @author rpuch
 */
@ReadingConverter
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {
    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnum(targetType);
    }

    private static class StringToEnum<T extends Enum<T>> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                // It's an empty enum identifier: reset the enum value to null.
                return null;
            }

            return new ClassMethods(enumType).stream()
                    .filter(this::isFromStorageMethod)
                    .findAny()
                    .map(fromStorageMethod -> invokeFromStorageMethod(source, fromStorageMethod))
                    .orElseGet(() -> convertViaEnumName(source));
        }

        @SuppressWarnings("unchecked")
        private T invokeFromStorageMethod(String source, Method fromStorageMethod) {
            return (T) ReflectionUtils.invokeMethod(fromStorageMethod, null, source);
        }

        private boolean isFromStorageMethod(Method method) {
            return method.isAnnotationPresent(FromStorageString.class)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == String.class;
        }

        private T convertViaEnumName(String source) {
            return Enum.valueOf(this.enumType, source.trim());
        }
    }
}
