package io.extremum.graphql.model;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PagingAndSoringRequestValidator {

    private final Map<Class<?>, Set<String>> classToMappedColumns = new HashMap<>();

    public void validate(PagingAndSortingRequest pagingAndSortingRequest, Class<?> clazz) {
        Set<String> collect = classToMappedColumns.computeIfAbsent(clazz, this::getMappedColumnsForClass);
        List<String> orderProperties = pagingAndSortingRequest.getOrders()
                .stream()
                .map(SortOrder::getProperty)
                .filter(property -> !collect.contains(property))
                .collect(Collectors.toList());
        if (!orderProperties.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s does not have \"%s\" field(s)", clazz.getSimpleName(), String.join(",", orderProperties)));
        }

    }

    @NotNull
    private Set<String> getMappedColumnsForClass(Class<?> clazz) {
        CamelCaseToUnderscoresNamingStrategy namingStrategy = new CamelCaseToUnderscoresNamingStrategy();
        return getFields(clazz).stream()
                .filter(this::notTransient)
                .map(field -> {
                    Column annotation = field.getAnnotation(Column.class);
                    if (annotation != null && annotation.name() != null && !annotation.name().isEmpty()) {
                        return annotation.name();
                    }
                    return field.getName();
                })
                .map(s -> namingStrategy.toPhysicalColumnName(Identifier.toIdentifier(s), null).toString())
                .collect(Collectors.toSet());
    }

    private boolean notTransient(Field field) {
        return field.getAnnotation(Transient.class) == null && field.getAnnotation(org.springframework.data.annotation.Transient.class) == null;
    }

    private <T> List<Field> getFields(Class<T> t) {
        List<Field> fields = new ArrayList<>();
        Class<?> clazz = t;
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
