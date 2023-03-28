package io.extremum.common.utils;

import java.lang.reflect.*;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @author iPolyakov on 02.04.15.
 * don't touch this class
 */
public class ReflectionUtils {
    @SuppressWarnings("unchecked")
    public static Class getGenericParameterClass(final Class actualClass, final Class genericClass, final int parameterIndex) {
        if (!genericClass.isAssignableFrom(actualClass.getSuperclass())) {
            throw new IllegalArgumentException("Class " + genericClass.getName() + " is not a superclass of "
                    + actualClass.getName());
        }

        Stack<ParameterizedType> genericClasses = new Stack<>();

        Class clazz = actualClass;

        while (true) {
            Type genericSuperclass = clazz.getGenericSuperclass();
            boolean isParameterizedType = genericSuperclass instanceof ParameterizedType;
            if (isParameterizedType) {
                genericClasses.push((ParameterizedType) genericSuperclass);
            } else {
                genericClasses.clear();
            }
            Type rawType = isParameterizedType ? ((ParameterizedType) genericSuperclass).getRawType() : genericSuperclass;
            if (!rawType.equals(genericClass)) {
                clazz = clazz.getSuperclass();
            } else {
                break;
            }
        }

        Type result = genericClasses.pop().getActualTypeArguments()[parameterIndex];

        while (result instanceof TypeVariable && !genericClasses.empty()) {
            int actualArgumentIndex = getParameterTypeDeclarationIndex((TypeVariable) result);
            ParameterizedType type = genericClasses.pop();
            result = type.getActualTypeArguments()[actualArgumentIndex];
        }

        if (result instanceof TypeVariable) {
            throw new IllegalStateException("Unable to resolve type variable " + result + "."
                    + " Try to replace instances of parametrized class with its non-parameterized subtype");
        }

        if (result instanceof ParameterizedType) {
            result = ((ParameterizedType) result).getRawType();
        }

        if (result == null) {
            throw new IllegalStateException("Unable to determine actual parameter type for "
                    + actualClass.getName());
        }

        if (!(result instanceof Class)) {
            throw new IllegalStateException("Actual parameter type for " + actualClass.getName() + " is not a Class");
        }

        return (Class) result;
    }

    private static int getParameterTypeDeclarationIndex(final TypeVariable typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();

        TypeVariable[] typeVariables = genericDeclaration.getTypeParameters();
        Integer actualArgumentIndex = null;
        for (int i = 0; i < typeVariables.length; i++) {
            if (typeVariables[i].equals(typeVariable)) {
                actualArgumentIndex = i;
                break;
            }
        }
        if (actualArgumentIndex != null) {
            return actualArgumentIndex;
        } else {
            throw new IllegalStateException("Argument " + typeVariable + " is not found in "
                    + genericDeclaration);
        }
    }

    public static Object getFieldValue(Field field, Object target) {
        field.setAccessible(true);

        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get field value", e);
        }
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        method.setAccessible(true);

        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot invoke a method", e);
        }
    }

    public static <T> T getFieldValue(Object target, String fieldName) {
        Field field = findExactlyOneField(target, fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked") T castValue = (T) getFieldValue(field, target);
        return castValue;
    }

    public static void setFieldValue(Object target, String fieldName, Object valueToSet) {
        Field field = findExactlyOneField(target, fieldName);
        field.setAccessible(true);
        setFieldValue(target, field, valueToSet);
    }

    private static Field findExactlyOneField(Object target, String fieldName) {
        List<Field> fields = new InstanceFields(target.getClass()).stream()
                .filter(field -> field.getName().equals(fieldName))
                .collect(Collectors.toList());
        if (fields.size() != 1) {
            String message = String.format("Expected to find exactly 1 field named '%s' but found %d",
                    fieldName, fields.size());
            throw new IllegalStateException(message);
        }
        return fields.get(0);
    }

    private static void setFieldValue(Object target, Field field, Object valueToSet) {
        try {
            field.set(target, valueToSet);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set field value", e);
        }
    }
}
