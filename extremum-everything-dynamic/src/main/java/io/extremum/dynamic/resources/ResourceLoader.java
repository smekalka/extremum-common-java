package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;

import java.io.InputStream;
import java.net.URI;

public interface ResourceLoader {
    InputStream loadAsInputStream(URI uri) throws ResourceLoadingException;
}
