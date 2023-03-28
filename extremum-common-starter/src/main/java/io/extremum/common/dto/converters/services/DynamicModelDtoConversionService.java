package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface DynamicModelDtoConversionService {
    Mono<ResponseDto> convertToResponseDtoReactively(Model model, ConversionConfig config);
}
