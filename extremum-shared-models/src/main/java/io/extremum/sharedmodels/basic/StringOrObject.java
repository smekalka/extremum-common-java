package io.extremum.sharedmodels.basic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import java.io.Serializable;

/**
 * @author rpuch
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class StringOrObject<T> implements Serializable {
    /**
     * What of fields of the {@link StringOrObject} is initialized
     */
    private Type type;
    private String string;
    @Valid
    private T object;

    private StringOrObject(Type type, String string, T object) {
        this.type = type;
        this.string = string;
        this.object = object;
    }

    public StringOrObject(String string) {
        this(Type.simple, string, null);
    }

    public StringOrObject(T object) {
        this(Type.complex, null, object);
    }

    public StringOrObject() {
        this(Type.unknown, null, null);
    }

    public boolean isComplex() {
        return type == Type.complex;
    }

    public boolean isSimple() {
        return type == Type.simple;
    }

    public boolean isKnown() {
        return type != Type.unknown;
    }

    public void makeSimple(String id) {
        type = Type.simple;
        this.string = id;
        object = null;
    }

    public void makeComplex(T object) {
        type = Type.complex;
        this.object = object;
        string = null;
    }

    public enum Type {
        unknown,
        /**
         * {@link StringOrObject#string} is initialized, {@link StringOrObject#object} are not
         */
        simple,

        /**
         * {@link StringOrObject#object} is initialized, {@link StringOrObject#string} are not
         */
        complex
    }
}
