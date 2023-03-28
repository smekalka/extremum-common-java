package io.extremum.dynamic.server.handlers.security.impl;

import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledOnOs(OS.WINDOWS)
class DefaultSchemaHandlerSecurityManagerTest {
    @Test
    void allowedForSimplePaths() {
        Path path = createFile("path/to/allowed/file");

        SchemaHandlerSecurityManager manager = new DefaultSchemaHandlerSecurityManager(getCurrentPaths().resolve("path/to/allowed"));

        boolean allowed = manager.isAccessAllowed(path);

        assertTrue(allowed);
    }

    @Test
    void disallowedForSimplePaths() {
        createFile("path/to/allowed/file");
        createFile("path/to/sensitive/resource");

        SchemaHandlerSecurityManager manager = new DefaultSchemaHandlerSecurityManager(getCurrentPaths().resolve("path/to/allowed"));

        boolean allowed = manager.isAccessAllowed(getCurrentPaths().resolve("path/to/sensitive/resource"));

        assertFalse(allowed);
    }

    @Test
    void allowedForPathsWithSymlinksAndDotsSequence() {
        createFile("path/to/resource");

        SchemaHandlerSecurityManager manager = new DefaultSchemaHandlerSecurityManager(getCurrentPaths().resolve("path/../path/to/"));

        boolean allowed = manager.isAccessAllowed(getCurrentPaths().resolve("path/to/../../path/to/resource"));

        assertTrue(allowed);
    }

    @Test
    void disallowedForPathsWithSymlinksAndDotsSequence() {
        createDirectory("path/to/directory");
        createFile("another/other");
        createSymbolicLink("path/to/directory", "another/place/slink");

        SchemaHandlerSecurityManager manager = new DefaultSchemaHandlerSecurityManager(getCurrentPaths()
                .resolve("another/place/slink"));

        assertFalse(manager.isAccessAllowed(getCurrentPaths().resolve("path/../another/other/resource")));
    }

    @SneakyThrows
    private Path createSymbolicLink(String target, String link) {
        Path resultSymlinkPath = getCurrentPaths().resolve(link);
        if (Files.exists(resultSymlinkPath)) {
            return resultSymlinkPath;
        } else {
            Path currentPaths = getCurrentPaths();

            String[] splitted = link.split("/");

            if (splitted.length > 1) {
                createDirectory(String.join("/", Arrays.copyOf(splitted, splitted.length - 1)));
            }

            return Files.createSymbolicLink(
                    currentPaths.resolve(link),
                    currentPaths.resolve(target)
            );
        }
    }

    @SneakyThrows
    private Path createFile(String path) {
        Path exists = getIfExists(path);

        if (exists != null) {
            return exists;
        } else {
            String[] splitted = path.split("/");
            if (splitted.length > 1) {
                createDirectory(String.join("/", Arrays.copyOf(splitted, splitted.length - 1)));
            }

            return Files.createFile(getCurrentPaths().resolve(path));
        }
    }

    @SneakyThrows
    private Path createDirectory(String path) {
        Path exists = getIfExists(path);
        if (exists != null) {
            return exists;
        } else {
            Path dynamicPath = getCurrentPaths();
            String[] splitted = path.split("/");
            for (String part : splitted) {
                dynamicPath = dynamicPath.resolve(part);

                Path partExists = getIfExists(dynamicPath);
                if (partExists == null) {
                    Files.createDirectory(dynamicPath);
                }
            }

            return dynamicPath;
        }
    }

    private Path getIfExists(String path) {
        Path resolved = getCurrentPaths().resolve(path);
        return getIfExists(resolved);
    }

    private Path getIfExists(Path path) {
        return Files.exists(path) ? path : null;
    }

    @SneakyThrows
    private Path getCurrentPaths() {
        URL resource = this.getClass().getClassLoader().getResource(".");
        return Paths.get(resource.toURI());
    }
}