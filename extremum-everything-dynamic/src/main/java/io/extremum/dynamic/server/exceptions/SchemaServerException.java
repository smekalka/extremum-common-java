package io.extremum.dynamic.server.exceptions;

public class SchemaServerException extends RuntimeException {
    public SchemaServerException(String message, Exception cause) {
        super(message, cause);
    }

    public SchemaServerException(String message) {
        super(message);
    }
}
