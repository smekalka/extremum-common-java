package io.extremum.common.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class DeclaredInstanceMethods {
    private final Class<?> targetClass;

    public DeclaredInstanceMethods(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Stream<Method> stream() {
        List<Method> methods = findDeclaredInstanceMethods();
        return methods.stream();
    }

    private List<Method> findDeclaredInstanceMethods() {
        List<Method> methods = new ArrayList<>();
        collectDeclaredInstanceMethods(targetClass, methods);
        return methods;
    }

    private void collectDeclaredInstanceMethods(Class<?> aClass, List<Method> methods) {
        Arrays.stream(aClass.getDeclaredMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isSynthetic())
                .forEach(methods::add);

        Class<?> superclass = aClass.getSuperclass();
        if (superclass != null) {
            collectDeclaredInstanceMethods(superclass, methods);
        }
    }
}
