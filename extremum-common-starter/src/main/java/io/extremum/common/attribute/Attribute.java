package io.extremum.common.attribute;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public interface Attribute {
    String name();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    default boolean isAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    Class<?> type();

    Object value();
}
