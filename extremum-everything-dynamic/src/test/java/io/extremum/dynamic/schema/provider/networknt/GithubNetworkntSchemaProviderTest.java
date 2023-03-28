package io.extremum.dynamic.schema.provider.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import io.extremum.dynamic.resources.github.GithubAccessOptions;
import io.extremum.dynamic.resources.github.GithubResourceConfiguration;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

import static io.extremum.dynamic.TestUtils.convertInputStreamToString;
import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@Testcontainers
class GithubNetworkntSchemaProviderTest {
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
    @Disabled("This test is flaky, it impedes a release. We must enable and fix it.")
    void loadSchemaFromGithub() throws IOException {
        // configure provider

        String owner = "john";
        String repo = "TestRepo";
        String schemaPath = "path/to/schemas";
        String ref = "release-2.0.3";

        GithubResourceConfiguration ghResConfigOrigin = new GithubResourceConfiguration(
                owner,
                repo,
                schemaPath,
                ref
        );

        GithubResourceConfiguration ghResConfig = Mockito.spy(ghResConfigOrigin);

        when(ghResConfig.getGithubApiBase())
                .thenReturn(format("http://%s:%d",
                        mockServerContainer.getContainerIpAddress(),
                        mockServerContainer.getServerPort()));

        String token = "AuthToken12345";

        GithubAccessOptions ghAccessOpts = new GithubAccessOptions(token);

        NetworkntSchemaProvider provider = new GithubNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                ghResConfig,
                ghAccessOpts
        );

        // configure mock server

        String schemaName = "like-network-access-schema";

        String mainSchema = convertInputStreamToString(
                loadResourceAsInputStream(this.getClass().getClassLoader(),
                        "schemas/like-network-access-schema")
        );

        mainSchema = "{\"content\":\"" + new String(Base64.getEncoder().encode(mainSchema.getBytes())) + "\"}";

        String secondSchema = convertInputStreamToString(
                loadResourceAsInputStream(this.getClass().getClassLoader(),
                        "schemas/path/second-like-network-schema")
        );

        secondSchema = "{\"content\":\"" + new String(Base64.getEncoder().encode(secondSchema.getBytes())) + "\"}";

        msClient.when(
                HttpRequest.request()
                        .withMethod(HttpMethod.GET.name())
                        .withHeader(HttpHeaders.AUTHORIZATION, "token " + token)
                        .withPath("/repos/" + owner + "/" + repo + "/contents/" + schemaPath + "/" + schemaName)
                        .withQueryStringParameter("ref", ref)
        ).respond(
                HttpResponse.response()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mainSchema)
        );

        msClient.when(
                HttpRequest.request()
                        .withMethod(HttpMethod.GET.name())
                        .withHeader(HttpHeaders.AUTHORIZATION, "token " + token)
                        .withPath("/repos/" + owner + "/" + repo + "/contents/" + schemaPath + "/path/second-like-network-schema")
                        .withQueryStringParameter("ref", ref)
        ).respond(
                HttpResponse.response()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(secondSchema)
        );

        // run test
        try {
            NetworkntSchema schema = provider.loadSchema(schemaName);
            Set<ValidationMessage> messages = schema.getSchema().validate(
                    new ObjectMapper()
                            .readValue("{\"field1\": 12, \"field3\": {\"externalField\": 12}}", JsonNode.class)
            );

            assertEquals(2, messages.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("MockServer logs: {}", mockServerContainer.getLogs());
            throw e;
        }
    }

    @Test
    void getSchemaTypeTest() {
        JsonSchemaType type = JsonSchemaType.V2019_09;

        GithubNetworkntSchemaProvider provider = new GithubNetworkntSchemaProvider(
                type,
                mock(GithubResourceConfiguration.class),
                mock(GithubAccessOptions.class)
        );

        assertEquals(type, provider.getSchemaType());
    }
}