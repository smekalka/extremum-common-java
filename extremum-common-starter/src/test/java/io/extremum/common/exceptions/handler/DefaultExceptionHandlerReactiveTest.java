package io.extremum.common.exceptions.handler;

import io.extremum.common.exceptions.CommonException;
import io.extremum.common.exceptions.DefaultExtremumExceptionHandlers;
import io.extremum.common.exceptions.handler.annotation.AnnotationBasedExtremumExceptionResolver;
import io.extremum.sharedmodels.dto.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;

public class DefaultExceptionHandlerReactiveTest {

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new TestController())
                .controllerAdvice(new DefaultExceptionHandler(new AnnotationBasedExtremumExceptionResolver(singletonList(
                        new DefaultExtremumExceptionHandlers()))))
                .build();
    }

    @Test
    public void whenNothingIsThrown_thenOriginalResponseShouldBeReturned() {
        webTestClient.get().uri("/ok")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("status").isEqualTo("ok")
                .jsonPath("code").isEqualTo(200)
                .jsonPath("result").isEqualTo("Success!");
    }

    @Test
    public void whenCommonExceptionIsThrown_thenCodeAndMessageFromCommonExceptionShouldBeReturnedInBody() {
        webTestClient.get().uri("/common-exception")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo("fail")
                .jsonPath("code").isEqualTo(403)
                .jsonPath("alerts[0].level").isEqualTo("error")
                .jsonPath("alerts[0].code").isEqualTo("403")
                .jsonPath("alerts[0].message").isEqualTo("Common exception message");
    }

    @Test
    public void whenCommonExceptionWithIncorrectCodeIsThrown_thenDefaultCodeAndMessageFromCommonExceptionShouldBeReturnedInBody() {
        webTestClient.get().uri("/common-exception-incorrect-code")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("status").isEqualTo("fail")
                .jsonPath("code").isEqualTo(500)
                .jsonPath("alerts[0].level").isEqualTo("error")
                .jsonPath("alerts[0].code").isEqualTo("500")
                .jsonPath("alerts[0].message").isEqualTo("Common exception incorrect code message");
    }

    @Test
    public void whenCommonExceptionIsThrownFromMono_thenCodeAndMessageFromCommonExceptionShouldBeReturnedInBody() {
        webTestClient.get().uri("/common-exception-from-mono")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PAYMENT_REQUIRED)
                .expectBody()
                .jsonPath("status").isEqualTo("fail")
                .jsonPath("code").isEqualTo(402)
                .jsonPath("alerts[0].level").isEqualTo("error")
                .jsonPath("alerts[0].code").isEqualTo("402")
                .jsonPath("alerts[0].message").isEqualTo("Common exception message from mono");
    }

    @RestController
    private static class TestController {
        @RequestMapping("/ok")
        Mono<Response> ok() {
            return Mono.just(Response.ok("Success!"));
        }

        @RequestMapping("/common-exception")
        Mono<Response> commonException() {
            throw new CommonException("Common exception message", 403);
        }

        @RequestMapping("/common-exception-incorrect-code")
        Mono<Response> commonExceptionIncorrectCode() {
            throw new CommonException("Common exception incorrect code message", 0);
        }

        @RequestMapping("/common-exception-from-mono")
        Mono<Response> commonExceptionFromMono() {
            return Mono.error(() -> new CommonException("Common exception message from mono", 402));
        }
    }

}
