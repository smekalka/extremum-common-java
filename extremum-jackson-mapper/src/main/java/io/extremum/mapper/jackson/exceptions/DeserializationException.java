package io.extremum.mapper.jackson.exceptions;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DeserializationException extends RuntimeException {
    @Getter
    private Map<String, String> errors;

    public DeserializationException(Map<String, String> errors) {
        this.errors = errors;
    }

    public DeserializationException(String field, String message) {
        addError(field, message);
    }

    private void addError(String field, String message) {
        if (errors == null) {
            errors = new HashMap<>();
        }

        errors.put(field, message);
    }
}
