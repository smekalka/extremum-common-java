package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;

@Slf4j
public class LocalResourceLoader implements ResourceLoader {
    @Override
    public InputStream loadAsInputStream(URI uri) throws ResourceNotFoundException {
        try {
            return new FileInputStream(Paths.get(uri).toFile());
        } catch (FileNotFoundException e) {
            log.error("File {} not found", uri, e);
            throw new ResourceNotFoundException(uri, e);
        }
    }
}
