package io.extremum.dynamic.server.supports;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public interface FilesSupportsService {
    /**
     * {@link java.nio.file.Files#isRegularFile(Path, LinkOption...)}
     */
    boolean isRegularFile(Path path);

    /**
     * {@link java.nio.file.Files#copy(Path, OutputStream)}
     */
    void copy(Path from, OutputStream to) throws IOException;
}
