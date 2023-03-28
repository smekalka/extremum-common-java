package io.extremum.jpa.repository;

import io.extremum.common.utils.DeclaredInstanceMethods;

import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author rpuch
 */
class JpaSoftDeletion {
    boolean supportsSoftDeletion(Class<?> domainType) {
        long nonTransientGetDeletedMethodsCount = new DeclaredInstanceMethods(domainType).stream()
                .filter(this::isPublic)
                .filter(method -> "getDeleted".equals(method.getName()))
                .filter(this::notAnnotatedAsTransient)
                .count();
        return nonTransientGetDeletedMethodsCount > 0;
    }

    private boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    private boolean notAnnotatedAsTransient(Method method) {
        return method.getAnnotation(Transient.class) == null;
    }
}
