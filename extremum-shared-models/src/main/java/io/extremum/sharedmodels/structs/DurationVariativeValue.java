package io.extremum.sharedmodels.structs;

public class DurationVariativeValue {
    public ValueType type;

    public String minStringValue;
    public Integer minIntValue;

    public String maxStringValue;
    public Integer maxIntValue;

    public String commonStringValue;
    public Integer commonIntValue;

    public DurationVariativeValue() {
    }

    public DurationVariativeValue(Object min, Object max) {
        type = ValueType.object;
        setMin(min);
        setMax(max);
    }

    public DurationVariativeValue(Object commonValue) {
        type = ValueType.simple;
        setCommonValue(commonValue);
    }

    public void setMin(Object o) {
        try {
            minIntValue = Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            minStringValue = o.toString();
        }
    }

    public void setMax(Object o) {
        try {
            maxIntValue = Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            maxStringValue = o.toString();
        }
    }

    public void setCommonValue(Object o) {
        try {
            commonIntValue = Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            commonStringValue = o.toString();
        }
    }

    public boolean isContainsObject() {
        return type.equals(ValueType.object);
    }

    public boolean isContainsMinValueInteger() {
        return minIntValue != null;
    }

    public boolean isContainsMinValueString() {
        return minStringValue != null;
    }

    public boolean isContainsMaxValueInteger() {
        return maxIntValue != null;
    }

    public boolean isContainsMaxValueString() {
        return maxStringValue != null;
    }

    public boolean isContainsCommonValueInteger() {
        return commonIntValue != null;
    }

    public boolean isContainsCommonValueString() {
        return commonStringValue != null;
    }

    public enum FIELDS {
        min, max
    }
}
