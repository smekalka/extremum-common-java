package io.extremum.dynamic.server.impl;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.extremum.dynamic.server.SchemaServer;
import io.extremum.dynamic.server.exceptions.SchemaServerInitializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
@RequiredArgsConstructor
public class HttpSchemaServer implements SchemaServer {
    private final int serverPort;
    private final String serverContextPath;
    private final HttpHandler httpHandler;
    private HttpServer server;

    private volatile boolean serverRunning = false;

    @Override
    public void launch() throws SchemaServerInitializationException {
        log.info("Trying to launch SchemaServer {}", this);
        if (serverRunning) {
            log.warn("SchemaServer {} already running on port {}", this, serverPort);
        } else {
            startServer();
        }
    }

    private void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext(serverContextPath, httpHandler);
            server.start();

            serverRunning = true;
            log.info("SchemaServer {} is running on context path {} and ready to connect on port {}",
                    this, serverContextPath, serverPort);
        } catch (IOException e) {
            log.error("SchemaServer can't be initialized", e);
            throw new SchemaServerInitializationException("Initialization server error", e);
        }
    }

    @Override
    public void shutdown() throws SchemaServerInitializationException {
        log.info("Shutdown server {}", this);
        server.stop(0);
        serverRunning = false;
    }

    @Override
    public boolean isRunning() {
        return serverRunning;
    }

    @Override
    public String toString() {
        return "DirectoryBasedSchemaServer{" +
                "serverPort=" + serverPort +
                '}';
    }
}
