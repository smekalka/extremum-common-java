package io.extremum.dynamic.resources;

import io.extremum.dynamic.TestUtils;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class LocalResourceLoaderTest {
    @Test
    void loadLocalResource() throws ResourceNotFoundException {
        String path = this.getClass().getClassLoader().getResource("test.file.txt").getPath();

        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        InputStream inputStream = resourceLoader.loadAsInputStream(URI.create("file:/").resolve(Paths.get(path).toString()));

        assertNotNull(inputStream);

        String textFromLocalResource = TestUtils.convertInputStreamToString(inputStream);
        assertNotNull(textFromLocalResource);
        assertEquals("abcd", textFromLocalResource);
    }

    @Test
    void loadUnknownResource_throwsResourceNotFoundException() {
        Path unknownPath = Paths.get("unknown_path");
        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        assertThrows(ResourceNotFoundException.class, () -> resourceLoader.loadAsInputStream(
                URI.create("file:/").resolve(unknownPath.toString())));
    }
}
