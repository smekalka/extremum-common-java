package io.extremum.common.utils;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public final class AnnotationUtils {
    public static <A extends Annotation> A findAnnotationDirectlyOrUnderProxy(Class<A> annotationClass,
            Class<?> targetClass) {
        A annotation = targetClass.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        final Class<?> classToCheckAnnotation = unwrapProxyClass(targetClass);
        return classToCheckAnnotation.getAnnotation(annotationClass);
    }

    private static Class<?> unwrapProxyClass(Class<?> modelClass) {
        if (EntityUtils.isProxyClass(modelClass)) {
            return modelClass.getSuperclass();
        } else {
            return modelClass;
        }
    }

    private AnnotationUtils() {
    }
}
