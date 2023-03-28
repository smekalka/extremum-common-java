package io.extremum.dynamic.server.exceptions;

import java.io.IOException;

public class SchemaServerInitializationException extends SchemaServerException {
    public SchemaServerInitializationException(String message, IOException cause) {
        super(message, cause);
    }
}
