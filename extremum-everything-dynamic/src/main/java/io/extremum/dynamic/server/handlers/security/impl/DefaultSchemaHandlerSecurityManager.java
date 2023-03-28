package io.extremum.dynamic.server.handlers.security.impl;

import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class DefaultSchemaHandlerSecurityManager implements SchemaHandlerSecurityManager {
    private final Path allowedArea;

    @Override
    public boolean isAccessAllowed(Path requestedPath) {
        try {
            return requestedPath.toRealPath().startsWith(allowedArea.toRealPath());
        } catch (IOException e) {
            log.error("Unable to check paths {}, {}", allowedArea, requestedPath, e);
            return false;
        }
    }
}
