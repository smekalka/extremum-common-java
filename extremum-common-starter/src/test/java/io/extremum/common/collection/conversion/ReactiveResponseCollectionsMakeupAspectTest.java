package io.extremum.common.collection.conversion;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveResponseCollectionsMakeupAspectTest {
    @InjectMocks
    private ReactiveResponseCollectionsMakeupAspect aspect;

    @Mock
    private CollectionMakeup makeup;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final Response response = Response.ok(responseDto);

    private TestController controllerProxy;

    @BeforeEach
    void prepareProxy() {
        controllerProxy = AspectWrapping.wrapInAspect(new TestController(responseDto, response), aspect);
    }

    @BeforeEach
    void configureMakeupToReturnEmptyMono() {
        lenient().when(makeup.applyCollectionMakeupReactively(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDto_whenCallingTheMethod_thenCollectionMakeupShouldBeApplied() {
        Response result = controllerProxy.returnsMonoWithResponseWithResponseDto().block();

        assertThat(result, is(sameInstance(response)));
        verifyThatMakeupWasApplied();

    }

    private void verifyThatMakeupWasApplied() {
        //noinspection UnassignedFluxMonoInstance
        verify(makeup).applyCollectionMakeupReactively(responseDto);
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDtoArray_whenCallingTheMethod_thenCollectionMakeupShouldBeApplied() {
        controllerProxy.returnsMonoWithResponseWithResponseDtoArray().block();

        verifyThatMakeupWasApplied();
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDtoList_whenCallingTheMethod_thenCollectionMakeupShouldBeApplied() {
        controllerProxy.returnsMonoWithResponseWithResponseDtoList().block();

        verifyThatMakeupWasApplied();
    }

    @Test
    void givenMethodReturnTypeIsMonoWithResponseAndMethodReturnsEmpty_whenCallingTheMethod_thenCollectionMakeupShouldNotBeApplied() {
        Response result = controllerProxy.returnsEmptyResponseMono().block();

        assertThat(result, is(nullValue()));
        verifyThatMakeupWasNotApplied();

    }

    private void verifyThatMakeupWasNotApplied() {
        //noinspection UnassignedFluxMonoInstance
        verify(makeup, never()).applyCollectionMakeupReactively(responseDto);
    }

    @Test
    void givenMethodReturnTypeIsMonoWithString_whenCallingTheMethod_thenCollectionMakeupShouldBeNotApplied() {
        String result = controllerProxy.returnsMonoWithString().block();

        assertThat(result, is("test"));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithString_whenCallingTheMethod_thenCollectionMakeupShouldBeNotApplied() {
        Response result = controllerProxy.returnsMonoWithResponseWithString().block();

        assertThat(result, is(notNullValue()));
        assertThat(result.getResult(), is("test"));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsMonoWithResponseAndItReturnsNull_whenCallingTheMethod_thenCollectionMakeupShouldNotBeApplied() {
        Mono<Response> result = controllerProxy.returnsNullMono();

        assertThat(result, is(nullValue()));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponse_whenCallingTheMethod_thenCollectionMakeupShouldBeApplied() {
        Response result = controllerProxy.returnsFluxWithResponse().blockLast();

        assertThat(result, is(sameInstance(response)));
        verifyThatMakeupWasApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponseAndMethodReturnsEmpty_whenCallingTheMethod_thenCollectionMakeupShouldBeApplied() {
        Response result = controllerProxy.returnsEmptyResponseFlux().blockLast();

        assertThat(result, is(nullValue()));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithString_whenCallingTheMethod_thenCollectionMakeupShouldBeNotApplied() {
        String result = controllerProxy.returnsFluxWithString().blockLast();

        assertThat(result, is("test"));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnsFluxWithResponseWithString_whenCallingTheMethod_thenCollectionMakeupShouldBeNotApplied() {
        Response result = controllerProxy.returnsFluxWithResponseWithString().blockLast();

        assertThat(result, is(notNullValue()));
        assertThat(result.getResult(), is("test"));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponseAndItReturnsNull_whenCallingTheMethod_thenCollectionMakeupShouldNotBeApplied() {
        Flux<Response> result = controllerProxy.returnsNullFlux();

        assertThat(result, is(nullValue()));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenMethodReturnsFluxOfServerSentEventsWithResponseDto_whenCallingTheMethod_thenCollectionMakeupShouldBeApplied() {
        ServerSentEvent<ResponseDto> result = controllerProxy.returnsFluxOfServerSentEventsWithResponseDto()
                .blockLast();
        assertThat(result, is(notNullValue()));

        assertThat(result.data(), is(sameInstance(responseDto)));
        verifyThatMakeupWasApplied();
    }

    @Test
    void givenMethodReturnsNonPublisher_whenCallingTheMethod_thenCollectionMakeupShouldNotBeApplied() {
        Response result = controllerProxy.returnsNonPublisher();

        assertThat(result, is(sameInstance(response)));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenTargetIsNotAController_whenInvokingAMonoMethodReturningAResponse_thenMakeupIsNotApplied() {
        NotAnnotatedAsController notController = AspectWrapping.wrapInAspect(
                new NotAnnotatedAsController(response), aspect);

        Response result = notController.returnsMonoWithResponse().block();

        assertThat(result, is(sameInstance(response)));
        verifyThatMakeupWasNotApplied();
    }

    @Test
    void givenTargetIsNotAController_whenInvokingAFluxMethodReturningAResponse_thenMakeupIsNotApplied() {
        NotAnnotatedAsController notController = AspectWrapping.wrapInAspect(
                new NotAnnotatedAsController(response), aspect);

        Response result = notController.returnsFluxWithResponse().blockLast();

        assertThat(result, is(sameInstance(response)));
        verifyThatMakeupWasNotApplied();
    }

}