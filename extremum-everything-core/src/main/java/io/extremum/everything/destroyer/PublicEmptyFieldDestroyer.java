package io.extremum.everything.destroyer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;

/**
 * Destroyer of empty PUBLIC fields in an object.
 * Will be helpful for destroying empty-object fields for clear serialization
 */
public class PublicEmptyFieldDestroyer implements EmptyFieldDestroyer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublicEmptyFieldDestroyer.class);

    private EmptyFieldDestroyerConfig config;

    public PublicEmptyFieldDestroyer() {
    }

    public PublicEmptyFieldDestroyer(EmptyFieldDestroyerConfig config) {
        this.config = config;
    }

    public EmptyFieldDestroyerConfig getConfig() {
        return config;
    }

    public void setConfig(EmptyFieldDestroyerConfig config) {
        this.config = config;
    }

    public <T> T destroy(T object) {
        Field[] fields = object.getClass().getFields();

        try {
            for (Field field : fields) {
                if (tryDestroy(field, object)) {
                    field.set(object, null);
                }
            }
        } catch (IllegalAccessException e) {
            String message = format("Can't destroy empty fields in %s", object);

            LOGGER.error(message, e);
            throw new RuntimeException(message);
        }

        return object;
    }

    private boolean tryDestroy(Field field, Object fieldOwner) throws IllegalAccessException {
        Object fieldValue = field.get(fieldOwner);
        if (fieldValue == null) {
            return true;
        }

        if (fieldValue instanceof Collection) {
            if (((Collection) fieldValue).isEmpty()) {
                return true;
            }

            return false;
        }

        if (!isAnalyzable(field, fieldOwner)) {
            return false;
        }

        Field[] fields = field.getType().getFields();

        boolean destroyed = true;
        for (Field childField : fields) {
            if (isAnalyzable(childField, fieldValue)) {
                if (tryDestroy(childField, fieldValue)) {
                    childField.set(fieldValue, null);
                } else {
                    destroyed = false;
                }
            } else {
                if (!isIgnorable(childField) && childField.get(fieldValue) != null) {
                    destroyed = false;
                }
            }
        }

        return destroyed;
    }

    private boolean isIgnorable(Field f) {
        if (f == null) {
            return true;
        }

        return Modifier.isStatic(f.getModifiers());
    }

    private boolean isAnalyzable(Field f, Object fieldOwner) throws IllegalAccessException {
        if (f == null) {
            return false;
        }

        Class<?> type = f.getType();

        if (type.isEnum()) {
            return false;
        }

        if (isAssignableFromPredicates(type)) {
            return false;
        }

        if (Modifier.isStatic(f.getModifiers())) {
            return false;
        }

        boolean inPackage = !Modifier.isStatic(f.getModifiers()) &&
                Optional.of(f)
                        .map(Field::getType)
                        .map(Class::getPackage)
                        .map(Package::getName)
                        .filter(this::isAnalyzablePackage)
                        .isPresent();

        if (inPackage) {
            return true;
        }

        Object value = f.get(fieldOwner);

        if (value == null) {
            return false;
        }

        return value instanceof Collection;
    }

    private boolean isAssignableFromPredicates(Class<?> type) {
        if (config == null) {
            return true;
        } else {
            List<Predicate<Class<?>>> predicates = config.getNotAnalyzableTypePredicates();
            if (predicates != null) {
                return predicates.stream().anyMatch(p -> p.test(type));
            } else {
                return false;
            }
        }
    }

    private boolean isAnalyzablePackage(String p) {
        if (config == null) {
            return true;
        } else {
            List<String> prefixes = config.getAnalyzablePackagePrefixes();
            if (prefixes != null) {
                for (String prefix : prefixes) {
                    if (p.startsWith(prefix)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
