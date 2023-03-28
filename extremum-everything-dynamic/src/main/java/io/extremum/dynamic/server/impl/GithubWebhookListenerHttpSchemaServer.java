package io.extremum.dynamic.server.impl;

import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.server.SchemaServer;
import io.extremum.dynamic.server.exceptions.SchemaServerInitializationException;
import io.extremum.dynamic.server.handlers.GithubWebhookServerHttpHandler;

import java.util.Collection;

public class GithubWebhookListenerHttpSchemaServer implements SchemaServer {
    private final int serverPort;
    private final String serverContextPath;
    private Collection<NetworkntCacheManager> cacheManagers;

    private SchemaServer server;

    public GithubWebhookListenerHttpSchemaServer(int serverPort, String serverContextPath, Collection<NetworkntCacheManager> cacheManagers) {
        this.serverPort = serverPort;
        this.serverContextPath = serverContextPath;
        this.cacheManagers = cacheManagers;
    }

    @Override
    public void launch() throws SchemaServerInitializationException {
        createServer();
    }

    private void createServer() {
        if (server == null) {
            GithubWebhookServerHttpHandler handler = new GithubWebhookServerHttpHandler(cacheManagers);
            server = new HttpSchemaServer(serverPort, serverContextPath, handler);
        }

        server.launch();

    }

    @Override
    public void shutdown() throws SchemaServerInitializationException {
        server.shutdown();
    }

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }
}
