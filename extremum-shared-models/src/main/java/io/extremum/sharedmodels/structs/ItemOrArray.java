package io.extremum.sharedmodels.structs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItemOrArray<T extends Serializable> {
    public Type type;
    public T item;
    public List<T> array;

    public ItemOrArray() {
        this(Type.unknown, null, new ArrayList<>());
    }

    public ItemOrArray(Type type, T item) {
        this(type, item, new ArrayList<>());
    }

    public ItemOrArray(Type type, List<T> array) {
        this(type, null, array);
    }

    public ItemOrArray(Type type, T item, List<T> array) {
        this.type = type;
        this.item = item;
        this.array = array;
    }

    public boolean isContainItem() {
        return type == Type.item;
    }

    public boolean isContainArray() {
        return type == Type.array;
    }

    public boolean isKnown() {
        return type != Type.unknown;
    }

    public enum Type {
        unknown,
        item,
        array
    }
}
