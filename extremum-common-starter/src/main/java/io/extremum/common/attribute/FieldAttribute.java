package io.extremum.common.attribute;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author rpuch
 */
public class FieldAttribute implements Attribute {
    private final Field field;
    private final Object value;

    public FieldAttribute(Field field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    @Override
    public Class<?> type() {
        return field.getType();
    }

    @Override
    public Object value() {
        return value;
    }
}
