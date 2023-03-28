package io.extremum.dynamic.resources.exceptions;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {
    @Test
    void getResourcePath() {
        URI uri = URI.create("path");
        ResourceNotFoundException ex = new ResourceNotFoundException(uri);

        assertEquals(uri.toString(), ex.getResourceUri().toString());
    }

    @Test
    void messageContainsAPath() {
        URI uri = URI.create("path");
        ResourceNotFoundException ex = new ResourceNotFoundException(uri);

        assertEquals("Resource wasn't found " + uri, ex.getMessage());
    }
}