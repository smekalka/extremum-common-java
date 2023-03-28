package io.extremum.sharedmodels.structs;

public class IntegerRangeOrValue {
    public ValueType type;

    public Integer min;
    public Integer max;
    public Integer value;

    public IntegerRangeOrValue() {
        type = ValueType.unknown;
    }

    public IntegerRangeOrValue(Integer value) {
        type = ValueType.integer;
        this.value = value;
    }

    public IntegerRangeOrValue(Integer min, Integer max) {
        type = ValueType.object;
        this.min = min;
        this.max = max;
    }

    public boolean isObject() {
        return type.equals(ValueType.object);
    }

    public boolean isInteger() {
        return type.equals(ValueType.integer);
    }

    public enum FIELDS {
        min, max
    }
}
