package io.extremum.dynamic;

import com.sun.net.httpserver.HttpHandler;
import io.extremum.dynamic.server.handlers.HttpSchemaServerHandler;
import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import io.extremum.dynamic.server.impl.HttpSchemaServer;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

@Slf4j
@Order(100) // launch directly before io.extremum.dynamic.DynamicModelHeater
@RequiredArgsConstructor
public class HttpSchemaServerLauncher implements ApplicationListener<ContextRefreshedEvent> {
    private final ExecutorService executor;
    private final Path schemaDirectory;
    private final int port;
    private final String context;
    private final FilesSupportsService fileSupportsService;
    private final SchemaHandlerSecurityManager securityManager;

    private volatile boolean alreadyLaunched = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!alreadyLaunched) {
            alreadyLaunched = true;

            HttpHandler handler = new HttpSchemaServerHandler(executor, schemaDirectory, fileSupportsService, securityManager);
            HttpSchemaServer server = new HttpSchemaServer(port, context, handler);
            server.launch();

            log.info("Launch SchemaServer {} with handler {}", server, handler);
        }
    }
}
