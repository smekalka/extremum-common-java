package io.extremum.dynamic.server;

import io.extremum.dynamic.server.exceptions.SchemaServerInitializationException;

public interface SchemaServer {
    void launch() throws SchemaServerInitializationException;

    void shutdown() throws SchemaServerInitializationException;

    boolean isRunning();
}
