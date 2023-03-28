package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;

@Slf4j
public class GithubWebhookServerExchangeHandler implements Runnable {
    private final HttpExchange httpExchange;
    private final Collection<NetworkntCacheManager> cacheManagers;

    public GithubWebhookServerExchangeHandler(HttpExchange httpExchange, Collection<NetworkntCacheManager> cacheManagers) {
        this.httpExchange = httpExchange;
        this.cacheManagers = cacheManagers;
    }

    @Override
    public void run() {
        try {
            // todo needs to remove only updated schema. But now I see a "ref problem" in this case
            cacheManagers.forEach(NetworkntCacheManager::clearCache);

            httpExchange.sendResponseHeaders(200, 0);
            httpExchange.getResponseBody().close();
        } catch (IOException e) {
            log.error("Unable to handle exchange", e);
        }
    }
}
