package io.extremum.common.collection.conversion;

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
@Order(30)
@RequiredArgsConstructor
public class ReactiveResponseCollectionsMakeupAspect extends ReactiveResponseDtoHandlingAspect {
    private final CollectionMakeup makeup;

    @Override
    protected Mono<?> applyToResponseDto(ResponseDto responseDto) {
        return makeup.applyCollectionMakeupReactively(responseDto);
    }
}