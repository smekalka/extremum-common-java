package io.extremum.dynamic.resources.exceptions;

import lombok.Getter;

import java.net.URI;

public class ResourceLoadingException extends RuntimeException {
    @Getter
    private URI resourceUri;

    public ResourceLoadingException(URI resource) {
        super("Unable to load resource " + resource.toString());
        this.resourceUri = resource;
    }

    public ResourceLoadingException(URI resource, Throwable cause) {
        super("Unable to load resource " + resource.toString(), cause);
        this.resourceUri = resource;
    }

    public ResourceLoadingException(String message, URI resource) {
        super(message);
        this.resourceUri = resource;
    }

    public ResourceLoadingException(String message, URI resource, Throwable cause) {
        super(message, cause);
        this.resourceUri = resource;
    }
}
