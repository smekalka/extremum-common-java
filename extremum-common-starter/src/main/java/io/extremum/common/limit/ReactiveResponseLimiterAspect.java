package io.extremum.common.limit;

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
@Order(20) // must be BEFORE ReactiveResponseCollectionsMakeupAspect so that limiting happen AFTER makeup
@RequiredArgsConstructor
public class ReactiveResponseLimiterAspect extends ReactiveResponseDtoHandlingAspect {
    private final ResponseLimiter limiter;

    @Override
    protected Mono<?> applyToResponseDto(ResponseDto responseDto) {
        return Mono.fromCallable(() -> {
            limiter.limit(responseDto);
            return Mono.empty();
        });
    }
}
