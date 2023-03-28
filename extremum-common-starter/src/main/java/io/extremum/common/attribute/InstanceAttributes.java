package io.extremum.common.attribute;

import io.extremum.common.utils.InstanceFields;
import io.extremum.common.utils.InstanceMethods;
import io.extremum.common.utils.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class InstanceAttributes {
    private final Object instance;

    public InstanceAttributes(Object instance) {
        this.instance = instance;
    }

    public Stream<Attribute> stream() {
        List<Attribute> attributes = findInstanceAttributes();
        return attributes.stream();
    }

    private List<Attribute> findInstanceAttributes() {
        Map<String, Field> fieldMap = new InstanceFields(instance.getClass()).stream()
                .collect(Collectors.toMap(Field::getName, field -> field));
        Map<String, Method> getterMap = new InstanceMethods(instance.getClass()).stream()
                .filter(this::notDeclaredByObjectClass)
                .filter(this::isGetter)
                .collect(Collectors.toMap(this::propertyName, method -> method));

        Set<String> attributeNames = new HashSet<>();
        attributeNames.addAll(fieldMap.keySet());
        attributeNames.addAll(getterMap.keySet());

        return attributeNames.stream()
                .map(attributeName -> buildAttribute(attributeName, fieldMap, getterMap))
                .collect(Collectors.toList());
    }

    private boolean notDeclaredByObjectClass(Method method) {
        return method.getDeclaringClass() != Object.class;
    }

    private boolean isGetter(Method method) {
        return hasGetterName(method) && hasNonVoidReturnType(method) && hasZeroParameters(method);
    }

    private boolean hasGetterName(Method method) {
        if (method.getName().startsWith("get")) {
            return true;
        }
        if (method.getName().startsWith("is") && method.getReturnType() == boolean.class) {
            return true;
        }
        return false;
    }

    private boolean hasNonVoidReturnType(Method method) {
        return method.getReturnType() != void.class;
    }

    private boolean hasZeroParameters(Method method) {
        return method.getParameterCount() == 0;
    }

    private String propertyName(Method method) {
        if (method.getName().startsWith("get")) {
            return stripPrefix(method.getName(), "get");
        } else if (method.getName().startsWith("is")) {
            return stripPrefix(method.getName(), "is");
        } else {
            throw new IllegalStateException(
                    String.format("A getter expected, but the prefix is not 'get' or 'is': '%s'", method));
        }
    }

    private String stripPrefix(String methodName, String prefix) {
        String base = methodName.substring(prefix.length());
        return StringUtils.uncapitalize(base);
    }

    private Attribute buildAttribute(String attributeName, Map<String, Field> fieldMap, Map<String, Method> getterMap) {
        Attribute fieldAttribute = null;
        if (fieldMap.containsKey(attributeName)) {
            Field field = fieldMap.get(attributeName);
            Object fieldValue = ReflectionUtils.getFieldValue(field, instance);
            fieldAttribute = new FieldAttribute(field, fieldValue);
        }

        Attribute getterAttribute = null;
        if (getterMap.containsKey(attributeName)) {
            getterAttribute = new GetterAttribute(attributeName, getterMap.get(attributeName), instance);
        }

        if (fieldAttribute != null && getterAttribute != null) {
            return new CompositeAttribute(getterAttribute, fieldAttribute);
        }
        if (fieldAttribute != null) {
            return fieldAttribute;
        }
        if (getterAttribute != null) {
            return getterAttribute;
        }

        throw new IllegalStateException(
                String.format("No attribute '%s' was found as either field or property", attributeName));
    }
}
