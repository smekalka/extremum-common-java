package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.mockito.Mockito.*;

class GithubWebhookServerExchangeHandlerTest {
    @Test
    void clearCaches() throws IOException {
        HttpExchange exhcange = mock(HttpExchange.class);

        OutputStream os = mock(OutputStream.class);
        when(exhcange.getResponseBody()).thenReturn(os);

        NetworkntCacheManager cm1 = mock(NetworkntCacheManager.class);
        NetworkntCacheManager cm2 = mock(NetworkntCacheManager.class);

        GithubWebhookServerExchangeHandler handler = new GithubWebhookServerExchangeHandler(exhcange, Arrays.asList(cm1, cm2));
        handler.run();

        verify(os).close();
        verify(exhcange).sendResponseHeaders(eq(HttpStatus.OK.value()), anyLong());
        verify(cm1).clearCache();
        verify(cm2).clearCache();
    }
}
