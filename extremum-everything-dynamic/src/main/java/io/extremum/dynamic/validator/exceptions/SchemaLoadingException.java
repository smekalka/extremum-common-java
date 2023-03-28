package io.extremum.dynamic.validator.exceptions;

public class SchemaLoadingException extends RuntimeException {
    public SchemaLoadingException(String message) {
        super("Unable to load schema: " + message);
    }

    public SchemaLoadingException(String message, Throwable cause) {
        super("Unable to load schema: " + message, cause);
    }
}
