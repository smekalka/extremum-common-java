package io.extremum.common.response.advice;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;

@Controller
@RequiredArgsConstructor
public class TestController {
    private final ResponseDto responseDto;
    private final Response response;

    public Mono<Response> returnsMonoWithResponseWithResponseDto() {
        return Mono.just(response);
    }

    public Mono<Response> returnsMonoWithResponseWithResponseDtoArray() {
        return Mono.just(Response.ok(new ResponseDto[]{responseDto}));
    }

    public Mono<Response> returnsMonoWithResponseWithResponseDtoList() {
        return Mono.just(Response.ok(singletonList(responseDto)));
    }

    public Mono<Response> returnsEmptyResponseMono() {
        return Mono.empty();
    }

    public Mono<String> returnsMonoWithString() {
        return Mono.just("test");
    }

    public Mono<Response> returnsMonoWithResponseWithString() {
        return Mono.just(Response.ok("test"));
    }

    public Mono<Response> returnsNullMono() {
        return null;
    }

    public Flux<Response> returnsFluxWithResponse() {
        return Flux.just(response);
    }

    public Flux<Response> returnsEmptyResponseFlux() {
        return Flux.empty();
    }

    public Flux<String> returnsFluxWithString() {
        return Flux.just("test");
    }

    public Flux<Response> returnsFluxWithResponseWithString() {
        return Flux.just(Response.ok("test"));
    }

    public Flux<Response> returnsNullFlux() {
        return null;
    }

    public Flux<ServerSentEvent<ResponseDto>> returnsFluxOfServerSentEventsWithResponseDto() {
        return Flux.just(
                ServerSentEvent.<ResponseDto>builder()
                        .data(responseDto)
                        .build()
        );
    }

    public Response returnsNonPublisher() {
        return response;
    }
}
