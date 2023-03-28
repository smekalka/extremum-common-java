package io.extremum.dynamic.server.handlers.security;

import java.nio.file.Path;

public interface SchemaHandlerSecurityManager {
    boolean isAccessAllowed(Path path);
}
