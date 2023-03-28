package io.extremum.dynamic.resources.exceptions;

import java.net.URI;

public class ResourceLoadingTimeoutException extends ResourceLoadingException {
    public ResourceLoadingTimeoutException(URI resource) {
        super(resource);
    }

    public ResourceLoadingTimeoutException(URI resource, Throwable cause) {
        super(resource, cause);
    }

    public ResourceLoadingTimeoutException(String message, URI resource) {
        super(message, resource);
    }

    public ResourceLoadingTimeoutException(String message, URI resource, Throwable cause) {
        super(message, resource, cause);
    }
}
