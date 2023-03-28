package io.extremum.dynamic.everything.dto;

import io.extremum.sharedmodels.dto.ResponseDto;

public interface DynamicModelResponseDto<Data> extends ResponseDto {
    Data getData();
}
