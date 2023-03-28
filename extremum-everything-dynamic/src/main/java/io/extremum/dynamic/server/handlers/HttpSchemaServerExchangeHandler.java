package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
public class HttpSchemaServerExchangeHandler implements Runnable {
    private final HttpExchange exchange;
    private final Path basicSchemaDirectory;
    private final FilesSupportsService filesSupportService;
    private final SchemaHandlerSecurityManager schemaHandlerSecurityManager;

    @Override
    public void run() {
        try {
            Path localPathToRequestedSchema = buildLocalPath(exchange);

            if (filesSupportService.isRegularFile(localPathToRequestedSchema)) {
                if (schemaHandlerSecurityManager.isAccessAllowed(localPathToRequestedSchema)) {
                    respondStatus(HttpStatus.OK);

                    exchange.getResponseHeaders()
                            .add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

                    filesSupportService.copy(localPathToRequestedSchema, exchange.getResponseBody());
                } else {
                    log.warn("Attempts to get access to restricted file {}", localPathToRequestedSchema);
                    respondAccessForbiddenError();
                }
            } else {
                log.warn("File can't be found on path {}", localPathToRequestedSchema);
                respondStatus(HttpStatus.NOT_FOUND);
            }

            exchange.getResponseBody().close();
        } catch (Exception e) {
            log.error("Exception occurred {}", e, e);
            respondInternalError();
        }
    }

    private Path buildLocalPath(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String contextPath = exchange.getHttpContext().getPath();

        String relative = requestPath.substring(contextPath.length());
        return Paths.get(basicSchemaDirectory.toString(), relative);
    }

    private void respondStatus(HttpStatus status) throws IOException {
        exchange.sendResponseHeaders(status.value(), 0);
    }

    private void respondAccessForbiddenError() {
        try {
            exchange.sendResponseHeaders(HttpStatus.FORBIDDEN.value(), 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            log.error("Unable to respond", e);
        }
    }

    private void respondInternalError() {
        try {
            exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            log.error("Unable to respond", e);
        }
    }
}
