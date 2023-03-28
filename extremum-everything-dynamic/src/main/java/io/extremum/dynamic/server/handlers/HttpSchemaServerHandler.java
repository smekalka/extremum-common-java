package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

@Slf4j
@ToString(exclude = {"executor", "filesSupportsService", "securityManager"})
@RequiredArgsConstructor
public class HttpSchemaServerHandler implements HttpHandler {
    private final ExecutorService executor;
    private final Path schemaDirectory;
    private final FilesSupportsService filesSupportsService;
    private final SchemaHandlerSecurityManager securityManager;

    @Override
    public void handle(HttpExchange httpExchange) {
        executor.submit(new HttpSchemaServerExchangeHandler(httpExchange, schemaDirectory, filesSupportsService, securityManager));
    }
}
