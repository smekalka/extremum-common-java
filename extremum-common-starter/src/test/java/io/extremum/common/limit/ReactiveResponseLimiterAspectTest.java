package io.extremum.common.limit;

import io.extremum.common.response.advice.NotAnnotatedAsController;
import io.extremum.common.response.advice.TestController;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.test.aop.AspectWrapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveResponseLimiterAspectTest {
    @InjectMocks
    private ReactiveResponseLimiterAspect aspect;

    @Mock
    private ResponseLimiter limiter;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final Response response = Response.ok(responseDto);

    private TestController controllerProxy;

    @BeforeEach
    void prepareProxy() {
        controllerProxy = AspectWrapping.wrapInAspect(new TestController(responseDto, response), aspect);
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDto_whenCallingTheMethod_thenLimitingShouldBeApplied() {
        Response result = controllerProxy.returnsMonoWithResponseWithResponseDto().block();

        assertThat(result, is(sameInstance(response)));
        verifyThatLimitingWasApplied();

    }

    private void verifyThatLimitingWasApplied() {
        //noinspection UnassignedFluxMonoInstance
        verify(limiter).limit(responseDto);
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDtoArray_whenCallingTheMethod_thenLimitingShouldBeApplied() {
        controllerProxy.returnsMonoWithResponseWithResponseDtoArray().block();

        verifyThatLimitingWasApplied();
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDtoList_whenCallingTheMethod_thenLimitingShouldBeApplied() {
        controllerProxy.returnsMonoWithResponseWithResponseDtoList().block();

        verifyThatLimitingWasApplied();
    }

    @Test
    void givenMethodReturnTypeIsMonoWithResponseAndMethodReturnsEmpty_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        Response result = controllerProxy.returnsEmptyResponseMono().block();

        assertThat(result, is(nullValue()));
        verifyThatLimitingWasNotApplied();

    }

    private void verifyThatLimitingWasNotApplied() {
        //noinspection UnassignedFluxMonoInstance
        verify(limiter, never()).limit(responseDto);
    }

    @Test
    void givenMethodReturnTypeIsMonoWithString_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        String result = controllerProxy.returnsMonoWithString().block();

        assertThat(result, is("test"));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithString_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        Response result = controllerProxy.returnsMonoWithResponseWithString().block();

        assertThat(result, is(notNullValue()));
        assertThat(result.getResult(), is("test"));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsMonoWithResponseAndItReturnsNull_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        Mono<Response> result = controllerProxy.returnsNullMono();

        assertThat(result, is(nullValue()));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponse_whenCallingTheMethod_thenLimitingShouldBeApplied() {
        Response result = controllerProxy.returnsFluxWithResponse().blockLast();

        assertThat(result, is(sameInstance(response)));
        verifyThatLimitingWasApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponseAndMethodReturnsEmpty_whenCallingTheMethod_thenLimitingShouldBeApplied() {
        Response result = controllerProxy.returnsEmptyResponseFlux().blockLast();

        assertThat(result, is(nullValue()));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithString_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        String result = controllerProxy.returnsFluxWithString().blockLast();

        assertThat(result, is("test"));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnsFluxWithResponseWithString_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        Response result = controllerProxy.returnsFluxWithResponseWithString().blockLast();

        assertThat(result, is(notNullValue()));
        assertThat(result.getResult(), is("test"));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponseAndItReturnsNull_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        Flux<Response> result = controllerProxy.returnsNullFlux();

        assertThat(result, is(nullValue()));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenMethodReturnsFluxOfServerSentEventsWithResponseDto_whenCallingTheMethod_thenLimitingShouldBeApplied() {
        ServerSentEvent<ResponseDto> result = controllerProxy.returnsFluxOfServerSentEventsWithResponseDto()
                .blockLast();
        assertThat(result, is(notNullValue()));

        assertThat(result.data(), is(sameInstance(responseDto)));
        verifyThatLimitingWasApplied();
    }

    @Test
    void givenMethodReturnsNonPublisher_whenCallingTheMethod_thenLimitingShouldNotBeApplied() {
        Response result = controllerProxy.returnsNonPublisher();

        assertThat(result, is(sameInstance(response)));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenTargetIsNotAController_whenInvokingAMonoMethodReturningAResponse_thenLimitingShouldNotBeApplied() {
        NotAnnotatedAsController notController = AspectWrapping.wrapInAspect(
                new NotAnnotatedAsController(response), aspect);

        Response result = notController.returnsMonoWithResponse().block();

        assertThat(result, is(sameInstance(response)));
        verifyThatLimitingWasNotApplied();
    }

    @Test
    void givenTargetIsNotAController_whenInvokingAFluxMethodReturningAResponse_thenLimitingShouldNotBeApplied() {
        NotAnnotatedAsController notController = AspectWrapping.wrapInAspect(
                new NotAnnotatedAsController(response), aspect);

        Response result = notController.returnsFluxWithResponse().blockLast();

        assertThat(result, is(sameInstance(response)));
        verifyThatLimitingWasNotApplied();
    }
}