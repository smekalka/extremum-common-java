package io.extremum.dynamic.resources.exceptions;

import java.net.URI;

public class AccessForbiddenResourceLoadingException extends ResourceLoadingException {
    public AccessForbiddenResourceLoadingException(URI resource) {
        super(resource);
    }

    public AccessForbiddenResourceLoadingException(URI resource, Throwable cause) {
        super(resource, cause);
    }
}
