package io.extremum.common.limit;

import io.extremum.common.response.advice.ResponseDtoHandlingAdvice;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author rpuch
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ResponseLimiterAdvice extends ResponseDtoHandlingAdvice {
    private final ResponseLimiter limiter;

    @Override
    protected void applyToResponseDto(ResponseDto responseDto) {
        limiter.limit(responseDto);
    }
}
