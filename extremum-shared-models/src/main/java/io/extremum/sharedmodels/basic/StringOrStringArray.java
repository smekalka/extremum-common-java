package io.extremum.sharedmodels.basic;

import java.util.List;

public class StringOrStringArray {

    private StringOrStringArray.Type type;
    private String string;
    private List<String> array;

    private StringOrStringArray(StringOrStringArray.Type type, String string, List<String> array) {
        this.type = type;
        this.string = string;
        this.array = array;
    }

    public StringOrStringArray(String string) {
        this(StringOrStringArray.Type.string, string, null);
    }

    public StringOrStringArray(List<String> object) {
        this(Type.array, null, object);
    }

    public StringOrStringArray() {
        this(StringOrStringArray.Type.unknown, null, null);
    }

    public boolean isArray() {
        return type == Type.array;
    }

    public boolean isString() {
        return type == Type.string;
    }

    public boolean isKnown() {
        return type != StringOrStringArray.Type.unknown;
    }

    public void makeString(String string) {
        type = Type.string;
        this.string = string;
        array = null;
    }

    public void makeArray(List<String> array) {
        type = Type.array;
        this.array = array;
        string = null;
    }

    public enum Type {
        unknown, string, array
    }
}
