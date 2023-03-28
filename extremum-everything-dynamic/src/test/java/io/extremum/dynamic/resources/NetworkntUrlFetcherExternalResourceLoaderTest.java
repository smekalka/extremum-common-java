package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.net.URI;

import static io.extremum.dynamic.TestUtils.convertInputStreamToString;
import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Testcontainers
class NetworkntUrlFetcherExternalResourceLoaderTest {
    @Container
    static MockServerContainer mockServerContainer = new MockServerContainer();

    static MockServerClient msClient;

    @BeforeAll
    static void beforeAll() {
        msClient = new MockServerClient(
                mockServerContainer.getContainerIpAddress(),
                mockServerContainer.getServerPort()
        );
    }

    @Test
    void resourceLoaded() {
        ResourceLoader loader = new NetworkntUrlFetcherExternalResourceLoader();

        String host = format("http://%s:%d",
                mockServerContainer.getContainerIpAddress(), mockServerContainer.getServerPort());
        String path = "/path/to/resource";

        InputStream localResourceFileIs = loadResourceAsInputStream(this.getClass().getClassLoader(), "test.file.txt");
        String localResourceContent = convertInputStreamToString(localResourceFileIs);

        msClient.when(
                request()
                        .withPath(path)
        ).respond(
                response()
                        .withBody(localResourceContent)
        );

        InputStream is = loader.loadAsInputStream(URI.create(host).resolve(path));
        String receivedContent = convertInputStreamToString(is);

        assertEquals(localResourceContent, receivedContent);
    }

    @Test
    void resourceNotFoundException_if_ResourceDoesntExists() {
        ResourceLoader loader = new NetworkntUrlFetcherExternalResourceLoader();

        String host = format("http://%s:%d",
                mockServerContainer.getContainerIpAddress(), mockServerContainer.getServerPort());
        String path = "/path/to/unknown/resource";

        msClient.when(
                request()
                        .withPath(path)
        ).respond(
                response()
                        .withStatusCode(HttpStatus.NOT_FOUND.value())
        );

        assertThrows(ResourceNotFoundException.class,
                () -> loader.loadAsInputStream(URI.create(host).resolve(path)));
    }
}