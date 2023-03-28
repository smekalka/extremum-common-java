package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum  AlertLevelEnum {
    TRACE("trace"),
    DEBUG("debug"),
    INFO("info"),
    WARNING("warn"),
    ERROR("error"),
    FATAL("fatal");

    private final String value;

    AlertLevelEnum (String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @JsonCreator
    public static AlertLevelEnum fromString(String value) {
        if (value != null) {
            for (AlertLevelEnum object : AlertLevelEnum.values()) {
                if (value.equalsIgnoreCase(object.value)) {
                    return object;
                }
            }
        }

        return null;
    }
}
