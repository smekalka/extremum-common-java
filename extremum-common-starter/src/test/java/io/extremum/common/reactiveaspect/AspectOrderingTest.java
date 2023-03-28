package io.extremum.common.reactiveaspect;

import io.extremum.common.collection.conversion.CollectionMakeup;
import io.extremum.common.collection.conversion.ReactiveResponseCollectionsMakeupAspect;
import io.extremum.common.descriptor.resolve.ReactiveDescriptorResolvingAspect;
import io.extremum.common.descriptor.resolve.ResponseDtoDescriptorResolver;
import io.extremum.common.limit.ReactiveResponseLimiterAspect;
import io.extremum.common.limit.ResponseLimiter;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.test.aop.AspectWrapping;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspectOrderingTest {
    @InjectMocks
    private ReactiveResponseCollectionsMakeupAspect applyMakeupAspect;
    @Mock
    private CollectionMakeup makeup;

    @InjectMocks
    private ReactiveResponseLimiterAspect limiterAspect;
    @Mock
    private ResponseLimiter limiter;

    @InjectMocks
    private ReactiveDescriptorResolvingAspect descriptorResolvingAspect;
    @Mock
    private ResponseDtoDescriptorResolver descriptorResolver;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final Response response = Response.ok(responseDto);

    private TestController controllerProxy;

    @BeforeEach
    void prepareProxy() {
        controllerProxy = AspectWrapping.wrapInAspects(new TestController(),
                applyMakeupAspect, limiterAspect, descriptorResolvingAspect);
    }

    @BeforeEach
    void configureMakeupToReturnEmptyMono() {
        lenient().when(makeup.applyCollectionMakeupReactively(any()))
                .thenReturn(Mono.empty());
    }

    @BeforeEach
    void configureDescriptorResolverToReturnEmptyMono() {
        lenient().when(descriptorResolver.resolveExternalIdsIn(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void makeupCollectionShouldBeRunBeforeLimiting() {
        controllerProxy.returnsMonoWithResponseWithResponseDto().block();

        InOrder inOrder = inOrder(makeup, limiter);

        inOrder.verify(makeup).applyCollectionMakeupReactively(responseDto);
        inOrder.verify(limiter).limit(responseDto);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void limitingShouldBeRunBeforeResolvingExternalId() {
        controllerProxy.returnsMonoWithResponseWithResponseDto().block();

        InOrder inOrder = inOrder(limiter, descriptorResolver);

        inOrder.verify(limiter).limit(responseDto);
        inOrder.verify(descriptorResolver).resolveExternalIdsIn(responseDto);
        inOrder.verifyNoMoreInteractions();
    }

    @Controller
    @NoArgsConstructor
    private class TestController {
        Mono<Response> returnsMonoWithResponseWithResponseDto() {
            return Mono.just(response);
        }
    }
}
