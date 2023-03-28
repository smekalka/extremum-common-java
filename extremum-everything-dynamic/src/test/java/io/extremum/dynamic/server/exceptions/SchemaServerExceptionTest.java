package io.extremum.dynamic.server.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaServerExceptionTest {
    @Test
    void getMessageTest() {
        String msg = "message";
        SchemaServerException ex = new SchemaServerException(msg);

        assertEquals(msg, ex.getMessage());
    }

    @Test
    void getCauseTest() {
        Exception cause = new Exception();
        String msg = "message";

        SchemaServerException ex = new SchemaServerException(msg, cause);

        assertEquals(msg, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}