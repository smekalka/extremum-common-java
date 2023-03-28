package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import io.extremum.dynamic.TestUtils;
import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import io.extremum.dynamic.server.supports.impl.DefaultFilesSupportsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HttpSchemaServerExchangeHandlerTest {
    @Test
    void respondWithFileContent() throws IOException {
        SchemaHandlerSecurityManager securityManager = mock(SchemaHandlerSecurityManager.class);
        doReturn(true).when(securityManager).isAccessAllowed(any());

        HttpExchange exchange = mock(HttpExchange.class);

        ByteArrayOutputStream responseBody = spy(ByteArrayOutputStream.class);
        HttpContext httpContext = mock(HttpContext.class);
        doReturn("/").when(httpContext).getPath();

        String fileName = "simple.schema.json";
        URI requestUri = URI.create(format("http://localhost:8080/%s", fileName));

        Headers responseHeaders = new Headers();

        doNothing().when(responseBody).close();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getRequestURI()).thenReturn(requestUri);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getHttpContext()).thenReturn(httpContext);

        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        Path basePath = Paths.get(this.getClass().getClassLoader().getResource("schemas").getPath());

        FilesSupportsService fileSupportsService = new DefaultFilesSupportsService();
        HttpSchemaServerExchangeHandler handler = new HttpSchemaServerExchangeHandler(exchange, basePath,
                fileSupportsService, securityManager);

        handler.run();

        // response output stream is closed
        verify(responseBody).close();

        // headers with status code was sent
        verify(exchange).sendResponseHeaders(codeCaptor.capture(), anyLong());

        InputStream contentIs = TestUtils.loadResourceAsInputStream(this.getClass().getClassLoader(), format("schemas/%s", fileName));

        // check response content
        String expectedContent = TestUtils.convertInputStreamToString(contentIs);
        String actualContent = new String(responseBody.toByteArray());
        assertEquals(expectedContent, actualContent);

        // check content-type
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseHeaders.get(HttpHeaders.CONTENT_TYPE).get(0));

        // check response code
        assertEquals(HttpStatus.OK.value(), (int) codeCaptor.getValue());
    }

    @Test
    void respond_404_ifRequestedResourceNotFound() throws IOException {
        SchemaHandlerSecurityManager securityManager = mock(SchemaHandlerSecurityManager.class);
        doReturn(true).when(securityManager).isAccessAllowed(any());

        HttpExchange exchange = mock(HttpExchange.class);

        Path baseDirectory = Paths.get("unknown_directory");

        OutputStream responseOutputStream = mock(OutputStream.class);

        HttpContext httpContext = mock(HttpContext.class);
        doReturn("/").when(httpContext).getPath();

        doNothing().when(responseOutputStream).close();

        when(exchange.getRequestURI()).thenReturn(URI.create("http://localhost/"));
        when(exchange.getResponseHeaders()).thenReturn(mock(Headers.class));
        when(exchange.getResponseBody()).thenReturn(responseOutputStream);
        when(exchange.getHttpContext()).thenReturn(httpContext);

        FilesSupportsService fileSupportsService = new DefaultFilesSupportsService();
        HttpSchemaServerExchangeHandler handler = new HttpSchemaServerExchangeHandler(exchange, baseDirectory,
                fileSupportsService, securityManager);
        handler.run();

        ArgumentCaptor<Integer> captorCode = ArgumentCaptor.forClass(Integer.class);
        verify(exchange).sendResponseHeaders(captorCode.capture(), anyLong());

        assertEquals(HttpStatus.NOT_FOUND.value(), (int) captorCode.getValue());
        verify(responseOutputStream).close();
    }
}