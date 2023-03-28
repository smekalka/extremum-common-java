package io.extremum.dynamic.server.supports.impl;

import io.extremum.dynamic.server.supports.FilesSupportsService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFilesSupportsService implements FilesSupportsService {
    @Override
    public boolean isRegularFile(Path path) {
        return Files.isRegularFile(path);
    }

    @Override
    public void copy(Path from, OutputStream to) throws IOException {
        Files.copy(from, to);
    }
}
