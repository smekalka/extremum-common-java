package io.extremum.common.response.advice;

import io.extremum.sharedmodels.dto.Response;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class NotAnnotatedAsController {
    private final Response response;

    public Mono<Response> returnsMonoWithResponse() {
        return Mono.just(response);
    }

    public Flux<Response> returnsFluxWithResponse() {
        return Flux.just(response);
    }
}
