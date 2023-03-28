package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GithubWebhookServerHttpHandler implements HttpHandler {
    private final Collection<NetworkntCacheManager> cacheManagers;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public GithubWebhookServerHttpHandler(Collection<NetworkntCacheManager> cacheManagers) {

        this.cacheManagers = cacheManagers;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        executorService.submit(new GithubWebhookServerExchangeHandler(httpExchange, cacheManagers));
    }
}
