package io.extremum.sharedmodels.basic;


import lombok.Getter;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.function.Function;

@Getter
public class IdOrObject<ID extends Serializable, T> {
    public IdOrObject(Type type, ID id, T object) {
        this.type = type;
        this.id = id;
        this.object = object;
    }

    public IdOrObject(ID id) {
        this(Type.simple, id, null);
    }

    public IdOrObject(T object) {
        this(Type.complex, null, object);
    }

    public IdOrObject() {
        this(Type.unknown, null, null);
    }

    public static <ID extends Serializable, T> IdOrObject<ID, T> forId(ID id) {
        return new IdOrObject<>(id);
    }

    public static <ID extends Serializable, T> IdOrObject<ID, T> forObject(T object) {
        return new IdOrObject<>(object);
    }

    /**
     * Who of fields of the {@link IdOrObject} is initialized
     */
    public Type type;
    public ID id;
    @Valid
    public T object;

    public boolean isComplex() {
        return type == Type.complex;
    }

    public boolean isSimple() {
        return type == Type.simple;
    }

    public boolean isKnown() {
        return type != Type.unknown;
    }

    public void makeSimple(ID id) {
        type = Type.simple;
        this.id = id;
        object = null;
    }

    public void makeComplex(T object) {
        type = Type.complex;
        this.object = object;
        id = null;
    }

    public String getInternalIdOrNull(Function<? super ID, String> idConverter) {
        if (isSimple()) {
            return idConverter.apply(id);
        }

        return null;
    }

    public enum Type {
        unknown,
        /**
         * {@link IdOrObject#id} is initialized, {@link IdOrObject#object} are not
         */
        simple,

        /**
         * {@link IdOrObject#object} is initialized, {@link IdOrObject#id} are not
         */
        complex
    }
}
