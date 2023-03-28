package io.extremum.everything.services.collection;

import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rpuch
 */
class IdConversion {
    private final Map<FromToKey, Converter> converters;

    IdConversion() {
        converters = new HashMap<>();

        addConverter(String.class, String.class, string -> string);
        addConverter(String.class, ObjectId.class, obj -> new ObjectId(obj.toString()));
        addConverter(ObjectId.class, String.class, Object::toString);
        addConverter(ObjectId.class, ObjectId.class, objectId -> objectId);
    }

    private <S, T> void addConverter(Class<S> fromClass, Class<T> toClass, Converter converter) {
        converters.put(new FromToKey(fromClass, toClass), converter);
    }

    <T> T convert(Object value, Class<T> targetClass) {
        Class<?> sourceClass = value.getClass();
        Converter converter = converters.get(new FromToKey(sourceClass, targetClass));
        if (converter == null) {
            throw new IllegalArgumentException(String.format("Cannot convert from '%s' to '%s'", sourceClass, targetClass));
        }
        Object convertedValue = converter.convert(value);
        return targetClass.cast(convertedValue);
    }

    private interface Converter {
        Object convert(Object value);
    }

    private static class FromToKey {
        private final Class<?> from;
        private final Class<?> to;

        private FromToKey(Class<?> from, Class<?> to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FromToKey fromToKey = (FromToKey) o;
            return Objects.equals(from, fromToKey.from) &&
                    Objects.equals(to, fromToKey.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }
}
