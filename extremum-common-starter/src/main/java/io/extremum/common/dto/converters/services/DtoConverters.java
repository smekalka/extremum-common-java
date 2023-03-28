package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.*;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface DtoConverters {
    <M extends Model, D extends RequestDto>
    Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends RequestDto>
    Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends ResponseDto>
    Optional<ToResponseDtoConverter<M, D>> findToResponseDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends ResponseDto>
    Optional<ReactiveToResponseDtoConverter<M, D>> findReactiveToResponseDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends RequestDto>
    Optional<ReactiveToRequestDtoConverter<M, D>> findReactiveToRequestDtoConverter(Class<? extends M> modelClass);
}
