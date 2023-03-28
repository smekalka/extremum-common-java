package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface DtoConversionService {

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    Mono<ResponseDto> convertUnknownToResponseDtoReactively(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);

    <M extends Model, D extends RequestDto> M convertFromRequestDto(Map<String, Object> values, Class<M> modelClass);

    Mono<RequestDto> convertUnknownToRequestDtoReactively(Model model, ConversionConfig config);

    <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<M> modelClass, D dto);

    Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass);

    Class<? extends RequestDto> findReactiveRequestDtoType(Class<? extends Model> modelClass);
}
