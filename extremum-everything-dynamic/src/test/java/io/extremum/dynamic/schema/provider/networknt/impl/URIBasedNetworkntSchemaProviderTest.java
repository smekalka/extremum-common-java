package io.extremum.dynamic.schema.provider.networknt.impl;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.net.URI;

import static io.extremum.dynamic.TestUtils.convertInputStreamToString;
import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class URIBasedNetworkntSchemaProviderTest {
    @Container
    private static MockServerContainer mockServerContainer = new MockServerContainer();
    private static MockServerClient mockServerClient;

    @BeforeAll
    static void beforeAll() {
        mockServerClient = new MockServerClient(
                mockServerContainer.getContainerIpAddress(),
                mockServerContainer.getServerPort()
        );
    }

    @Test
    void loadSchema() {
        configureMockClient();

        URI schemaUri = buildUriTo("/schemas/");
        NetworkntSchemaProvider provider = new URIBasedNetworkntSchemaProvider(schemaUri);

        NetworkntSchema loaded = provider.loadSchema("like-network-access-schema");
        assertNotNull(loaded);
    }

    private URI buildUriTo(String path) {
        return URI.create(String.format("http://%s:%d", mockServerContainer.getContainerIpAddress(), mockServerContainer.getServerPort()))
                .resolve(path);
    }

    private void configureMockClient() {
        mockServerClient
                .when(HttpRequest.request()
                        .withPath("/schemas/like-network-access-schema"))
                .respond(HttpResponse.response()
                        .withBody(loadResource("schemas/like-network-access-schema")));

        mockServerClient
                .when(HttpRequest.request()
                        .withPath("/schemas/path/second-like-network-schema"))
                .respond(HttpResponse.response()
                        .withBody(loadResource("schemas/path/second-like-network-schema")));
    }

    private String loadResource(String resourceRelativePath) {
        InputStream is = loadResourceAsInputStream(this.getClass().getClassLoader(), resourceRelativePath);
        return convertInputStreamToString(is);
    }
}