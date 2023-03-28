package io.extremum.common.logging;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.logging.LoggingConstants;
import io.extremum.test.aop.AspectWrapping;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ReactiveFillRequestIdOnResponseAspectTest {
    private final ReactiveFillRequestIdOnResponseAspect aspect = new ReactiveFillRequestIdOnResponseAspect();

    private final String requestId = "12345";
    private final Context context = Context.of(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, requestId);

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final Response originalResponse = Response.ok(responseDto);

    private TestController controllerProxy;

    @BeforeEach
    void prepareProxy() {
        controllerProxy = AspectWrapping.wrapInAspect(new TestController(), aspect);
    }

    @Test
    void givenControllerMethodReturnsMonoWithResponse_whenApplyingAspect_thenRequestIdShouldBeFilledAndPayloadRemainTheSame() {
        Mono<Response> mono = controllerProxy.returnsMonoWithResponse()
                .subscriberContext(context);

        StepVerifier.create(mono)
                .assertNext(this::assertThatResponseRequestIdIsFilledAndThePayloadIsTheSame)
                .verifyComplete();
    }

    private void assertThatResponseRequestIdIsFilledAndThePayloadIsTheSame(Response returnedResponse) {
        assertThat(returnedResponse, is(notNullValue()));
        assertThat(returnedResponse.getRequestId(), is(requestId));
        assertThat(returnedResponse.getResult(), is(sameInstance(responseDto)));
    }

    @Test
    void givenControllerMethodReturnsNonPublisher_whenApplyingAspect_thenRequestShouldNotBeChanged() {
        Response returnedResponse = controllerProxy.returnsNonPublisher();

        assertThat(returnedResponse, is(sameInstance(originalResponse)));
    }

    @Test
    void givenTargetIsNotAController_whenApplyingAspect_thenRequestShouldNotBeChanged() {
        NotAnnotatedAsController notAnnotatedAsController = AspectWrapping.wrapInAspect(
                new NotAnnotatedAsController(), aspect);

        Mono<Response> mono = notAnnotatedAsController.returnsMonoWithResponse();

        StepVerifier.create(mono)
                .assertNext(returnedResponse -> {
                    assertThat(returnedResponse, is(sameInstance(originalResponse)));
                })
                .verifyComplete();
    }

    @Test
    void givenControllerAdviceMethodReturnsMonoWithResponse_whenApplyingAspect_thenRequestIdShouldBeFilledAndPayloadRemainTheSame() {
        TestControllerAdvice controllerAdviceProxy = AspectWrapping.wrapInAspect(
                new TestControllerAdvice(), aspect);

        Mono<Response> mono = controllerAdviceProxy.returnsMonoWithResponse()
                .subscriberContext(context);

        StepVerifier.create(mono)
                .assertNext(this::assertThatResponseRequestIdIsFilledAndThePayloadIsTheSame)
                .verifyComplete();
    }

    @Controller
    @NoArgsConstructor
    private class TestController {
        Mono<Response> returnsMonoWithResponse() {
            return Mono.just(originalResponse);
        }

        Response returnsNonPublisher() {
            return originalResponse;
        }
    }

    @NoArgsConstructor
    private class NotAnnotatedAsController {
        Mono<Response> returnsMonoWithResponse() {
            return Mono.just(originalResponse);
        }
    }

    @ControllerAdvice
    @NoArgsConstructor
    private class TestControllerAdvice {
        Mono<Response> returnsMonoWithResponse() {
            return Mono.just(originalResponse);
        }
    }
}