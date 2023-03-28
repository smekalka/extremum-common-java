package io.extremum.common.exceptions.end2end.disabled;

import io.extremum.common.exceptions.end2end.fixture.ReactiveExceptionsTestController;
import io.extremum.common.test.TestWithServices;
import io.extremum.starter.CommonConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {CommonConfiguration.class, DisabledHandlerExceptionTestConfiguration.class},
        properties = {"spring.main.web-application-type=reactive", "exceptions.disable-extremum-handlers=true"}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DisabledReactiveExtremumExceptionHandlerEndToEndTest extends TestWithServices {

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    @BeforeAll
    void beforeAll() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    public void whenCommonExceptionIsThrownFromFluxWithDataAndExceptionInSSE_exceptionShouldBeHandledWithSpringExceptionHandler() {
        Flux<ServerSentEvent<String>> response = webTestClient.get()
                .uri("/reactive-exceptions/flux-with-data-and-exception")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                }).getResponseBody();

        StepVerifier.create(response)
                .assertNext(sse -> assertSse(sse, 0))
                .assertNext(sse -> assertSse(sse, 1))
                .assertNext(sse -> assertSse(sse, 2))
                .assertNext(sse -> assertSse(sse, 3))
                .expectError()
                .verify();
    }

    @Test
    public void whenCommonExceptionIsThrownFromFluxWithDataAndExceptionInJson_exceptionShouldBeHandledWithSpringExceptionHandler() {
        Flux<String> response = webTestClient.get()
                .uri("/reactive-exceptions/flux-with-data-and-exception")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(response)
                .assertNext(data -> assertError(data, "SpringExceptionHandler: Common exception message", 405))
                .expectComplete()
                .verify();
    }

    @Test
    public void whenCommonExceptionIsThrownFromFluxWithExceptionInSSE_exceptionShouldBeHandledWithSpringExceptionHandler() {
        Flux<ServerSentEvent<String>> response = webTestClient.get()
                .uri("/reactive-exceptions/flux-with-exception")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                }).getResponseBody();

        StepVerifier.create(response)
                .assertNext(sse -> assertError(sse.data(), "SpringExceptionHandler: Common exception message", 405))
                .expectComplete()
                .verify();
    }

    @Test
    public void whenCommonExceptionIsThrownFromFluxWithExceptionInJson_exceptionShouldBeHandledWithSpringExceptionHandler() {
        Flux<String> response = webTestClient.get()
                .uri("/reactive-exceptions/flux-with-exception")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(response)
                .assertNext(data -> assertError(data, "SpringExceptionHandler: Common exception message", 405))
                .expectComplete()
                .verify();
    }

    private void assertSse(ServerSentEvent<String> sse, int index) {
        assertThat(sse.data()).isEqualTo(asString(index));
    }

    private void assertError(String data, String message, int code) {
        assertThat(data)
                .contains("\"message\":\"" + message + "\"")
                .contains("\"code\":" + code)
                .contains("\"status\":\"fail\"");
    }

    @SneakyThrows
    private String asString(int index) {
        return new ObjectMapper().writeValueAsString(ReactiveExceptionsTestController.DATA.get(index));
    }

}
