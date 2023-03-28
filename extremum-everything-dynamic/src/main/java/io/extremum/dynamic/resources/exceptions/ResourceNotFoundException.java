package io.extremum.dynamic.resources.exceptions;

import java.net.URI;

public class ResourceNotFoundException extends ResourceLoadingException {
    public ResourceNotFoundException(URI resourceUri) {
        super("Resource wasn't found " + resourceUri, resourceUri);

    }

    public ResourceNotFoundException(URI resourceUri, Throwable cause) {
        super("Resource wasn't found " + resourceUri, resourceUri, cause);
    }
}
