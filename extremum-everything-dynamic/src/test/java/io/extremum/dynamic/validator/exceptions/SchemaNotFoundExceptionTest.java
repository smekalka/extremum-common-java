package io.extremum.dynamic.validator.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaNotFoundExceptionTest {
    @Test
    void getSchemaName() {
        String schemaName = "schemaName";
        SchemaNotFoundException ex = new SchemaNotFoundException(schemaName);

        assertEquals(schemaName, ex.getSchemaName());
    }
}
