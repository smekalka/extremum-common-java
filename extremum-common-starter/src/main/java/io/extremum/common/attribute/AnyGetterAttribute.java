package io.extremum.common.attribute;

import io.extremum.common.utils.ReflectionUtils;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnyGetterAttribute implements Attribute {

    private final String attributeName;
    private final Method getter;
    private final Object target;

    public AnyGetterAttribute(String attributeName, Method getter, Object target) {
        this.attributeName = attributeName;
        this.getter = getter;
        this.target = target;
    }

    public String name() {
        return this.attributeName;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return this.getter.getAnnotation(annotationClass);
    }

    @SneakyThrows
    public Class<?> type() {
        return this.getter.invoke(target, attributeName).getClass();
    }

    public Object value() {
        return ReflectionUtils.invokeMethod(this.getter, this.target, attributeName);
    }
}