package io.extremum.dynamic.validator.exceptions;

import lombok.Getter;

public class SchemaNotFoundException extends SchemaLoadingException {
    @Getter
    private final String schemaName;

    public SchemaNotFoundException(String schemaName) {
        super("Schema " + schemaName + " not found");
        this.schemaName = schemaName;
    }

    public SchemaNotFoundException(String schemaName, Throwable cause) {
        super("Schema " + schemaName + " not found", cause);
        this.schemaName = schemaName;
    }
}
