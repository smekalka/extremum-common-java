package io.extremum.common.attribute;

import com.google.common.collect.ImmutableList;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
class CompositeAttribute implements Attribute {
    private final List<Attribute> attributes;

    CompositeAttribute(Attribute... attributes) {
        Objects.requireNonNull(attributes, "Attributes collection cannot be null");
        if (attributes.length == 0) {
            throw new IllegalArgumentException("Attributes collection cannot be empty");
        }

        this.attributes = ImmutableList.copyOf(attributes);
    }

    @Override
    public String name() {
        return firstAttribute().name();
    }

    private Attribute firstAttribute() {
        return attributes.get(0);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        List<A> annotations = attributes.stream()
                .map(attribute -> attribute.getAnnotation(annotationClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (annotations.isEmpty()) {
            return null;
        }

        if (annotations.size() > 1) {
            String message = String.format("There is both field and property with name '%s' annotated with '%s'",
                    name(), annotationClass);
            throw new IllegalStateException(message);
        }

        return annotations.get(0);
    }

    @Override
    public Class<?> type() {
        Set<? extends Class<?>> types = attributes.stream()
                .map(Attribute::type)
                .collect(Collectors.toSet());

        if (types.size() > 1) {
            String message = String.format("Field and property named '%s' have different types: '%s'",
                    name(), types);
            throw new IllegalStateException(message);
        }

        return types.iterator().next();
    }

    @Override
    public Object value() {
        return firstAttribute().value();
    }
}
