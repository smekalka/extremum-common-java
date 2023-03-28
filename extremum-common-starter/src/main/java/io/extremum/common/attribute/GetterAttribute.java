package io.extremum.common.attribute;

import io.extremum.common.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author rpuch
 */
class GetterAttribute implements Attribute {
    private final String attributeName;
    private final Method getter;
    private final Object target;

    GetterAttribute(String attributeName, Method getter, Object target) {
        this.attributeName = attributeName;
        this.getter = getter;
        this.target = target;
    }

    @Override
    public String name() {
        return attributeName;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getter.getAnnotation(annotationClass);
    }

    @Override
    public Class<?> type() {
        return getter.getReturnType();
    }

    @Override
    public Object value() {
        return ReflectionUtils.invokeMethod(getter, target);
    }
}
