package io.extremum.common.attribute;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.extremum.common.utils.InstanceMethods;
import io.extremum.sharedmodels.dto.AttributeGetter;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnyGetterAttributes {
    private final Object instance;

    public AnyGetterAttributes(Object instance) {
        this.instance = instance;
    }

    public Stream<Attribute> stream() {
        List<Attribute> attributes = findAnyGetterAttributes();
        return attributes.stream();
    }

    private List<Attribute> findAnyGetterAttributes() {
        Map<String, Method> anyGetterMap = new InstanceMethods(instance.getClass()).stream()
                .filter(this::notDeclaredByObjectClass)
                .filter(this::isAnyGetter)
                .collect(Collectors.toMap(this::propertyName, method -> method));

        return anyGetterMap.keySet()
                .stream()
                .flatMap(attributeName -> buildAttributes(attributeName, anyGetterMap, instance).stream())
                .collect(Collectors.toList());
    }

    private boolean notDeclaredByObjectClass(Method method) {
        return method.getDeclaringClass() != Object.class;
    }

    private boolean isAnyGetter(Method method) {
        return method.getAnnotation(JsonAnyGetter.class) != null;
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


    @SneakyThrows
    private List<Attribute> buildAttributes(String attributeName, Map<String, Method> anyGetterMethodMap, Object instance) {
        Optional<Method> getter = new InstanceMethods(instance.getClass())
                .stream().filter(method -> method.getAnnotation(AttributeGetter.class) != null)
                .findFirst();
        if (getter.isPresent()) {
            if (anyGetterMethodMap.containsKey(attributeName)) {
                Method method = anyGetterMethodMap.get(attributeName);
                Map<String, Object> anyGetterDataMap = (Map<String, Object>) method.invoke(instance);

                return anyGetterDataMap.entrySet()
                        .stream()
                        .filter(stringObjectEntry -> stringObjectEntry.getValue() != null)
                        .map(stringObjectEntry -> new AnyGetterAttribute(stringObjectEntry.getKey(), getter.get(), instance))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
