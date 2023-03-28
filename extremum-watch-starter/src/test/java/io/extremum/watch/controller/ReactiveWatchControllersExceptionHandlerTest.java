package io.extremum.watch.controller;

import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.watch.exception.WatchException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@WebFluxTest(value = ReactiveWatchControllersExceptionHandlerTest.TestController.class, properties =  "custom.watch.reactive=true")
@ContextConfiguration(classes = {
        WatchControllersTestConfiguration.class,
        ReactiveWatchController.class,
        ReactiveWatchControllerExceptionHandler.class,
        ReactiveWatchControllersExceptionHandlerTest.TestController.class
})
class ReactiveWatchControllersExceptionHandlerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenExtremumAccessDeniedExceptionIsThrown_thenProper403ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getAndParseResponse("/watch/extremum-access-denied-exception");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(403));
        assertThat(root.has("result"), is(false));
    }

    @NotNull
    private JSONObject getAndParseResponse(String uri) throws Exception {
        String result = webTestClient.get()
                .uri(uri)
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        return parseResponse(result);
    }

    @NotNull
    private JSONObject parseResponse(String content) throws UnsupportedEncodingException, JSONException {
        return new JSONObject(content);
    }

    @Test
    void whenWatchExceptionIsThrown_thenProper500ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getAndParseResponse("/watch/watch-exception");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(500));
        assertThat(root.has("result"), is(false));
    }

    @RestController
    @RequestMapping("/watch")
    public static class TestController {
        @RequestMapping("/ok")
        Mono<Response> ok() {
            return Mono.just(Response.ok("Success!"));
        }

        @RequestMapping("/watch-exception")
        Mono<Response> watchException() {
            return Mono.error(new WatchException("Something is wrong"));
        }

        @RequestMapping("/extremum-access-denied-exception")
        Mono<Response> extremumAccessDeniedException() {
            return Mono.error(new ExtremumAccessDeniedException("Access denied"));
        }
    }
}