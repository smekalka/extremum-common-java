package io.extremum.security.rules.model;

import java.util.HashMap;
import java.util.Map;

public enum AllowScope {
    READ("read"),
    WRITE("write"),
    UPDATE("update"),
    DELETE("delete"),
    WATCH("watch"),
    CREATE("create");

    private final String stringValue;
    private static final Map<String, AllowScope> stringToEnumMap = new HashMap<>();

    static {
        for (AllowScope enumConstant : AllowScope.class.getEnumConstants()) {
            stringToEnumMap.put(enumConstant.stringValue, enumConstant);
        }
    }

    AllowScope(String stringValue) {
        this.stringValue = stringValue;
    }

    public static AllowScope byStringValueIgnoringCase(String enumString) {
        return stringToEnumMap.get(enumString.toLowerCase());
    }
}