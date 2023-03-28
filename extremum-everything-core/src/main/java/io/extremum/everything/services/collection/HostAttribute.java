package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.utils.ModelUtils;
import io.extremum.everything.collection.CollectionElementType;
import io.extremum.everything.exceptions.EverythingEverythingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rpuch
 */
class HostAttribute {
    private final String name;
    private final Method getter;
    private final Field field;

    HostAttribute(String name, Method getter, Field field) {
        this.name = name;
        this.getter = getter;
        this.field = field;
    }

    public String name() {
        return name;
    }

    public Method getter() {
        return getter;
    }

    public Optional<Field> field() {
        return Optional.ofNullable(field);
    }

    Class<? extends Model> detectElementClass(Model host) {
        CollectionElementType fieldAnnotation = field()
                .map(currentField -> currentField.getAnnotation(CollectionElementType.class))
                .orElse(null);
        CollectionElementType getterAnnotation = getter().getAnnotation(CollectionElementType.class);

        throwIfNoCollectionElementTypeAnnotationIsPresent(host, fieldAnnotation, getterAnnotation);
        throwIfMultipleCollectionElementAnnotationIsPresent(host, fieldAnnotation, getterAnnotation);

        if (getterAnnotation != null) {
            return getterAnnotation.value();
        }

        Objects.requireNonNull(fieldAnnotation, "fieldAnnotation is null");
        return fieldAnnotation.value();
    }

    private void throwIfNoCollectionElementTypeAnnotationIsPresent(Model host, CollectionElementType fieldAnnotation,
            CollectionElementType getterAnnotation) {
        if (fieldAnnotation == null && getterAnnotation == null) {
            String name = ModelUtils.getModelName(host);
            String message = String.format(
                    "For host type '%s' attribute '%s' does not contain @CollectionElementType annotation",
                    name, name());
            throw new EverythingEverythingException(message);
        }
    }

    private void throwIfMultipleCollectionElementAnnotationIsPresent(Model host,
            CollectionElementType fieldAnnotation, CollectionElementType getterAnnotation) {
        if (fieldAnnotation != null && getterAnnotation != null) {
            String name = ModelUtils.getModelName(host);
            String message = String.format(
                    "For host type '%s' attribute '%s' has @CollectionElementType annotation on both field and getter",
                    name, name());
            throw new EverythingEverythingException(message);
        }
    }
}
