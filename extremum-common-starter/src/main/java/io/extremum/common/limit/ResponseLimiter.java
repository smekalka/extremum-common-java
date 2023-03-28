package io.extremum.common.limit;

import io.extremum.sharedmodels.dto.ResponseDto;

/**
 * @author rpuch
 */
public interface ResponseLimiter {
    void limit(ResponseDto responseDto);
}
