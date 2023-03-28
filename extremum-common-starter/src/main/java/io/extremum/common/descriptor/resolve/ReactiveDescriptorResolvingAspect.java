package io.extremum.common.descriptor.resolve;

import io.extremum.common.response.advice.ReactiveResponseDtoHandlingAspect;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@Aspect
@Order(10)
@RequiredArgsConstructor
public class ReactiveDescriptorResolvingAspect extends ReactiveResponseDtoHandlingAspect {
    private final ResponseDtoDescriptorResolver descriptorResolver;

    @Override
    protected Mono<?> applyToResponseDto(ResponseDto responseDto) {
        return descriptorResolver.resolveExternalIdsIn(responseDto);
    }
}
