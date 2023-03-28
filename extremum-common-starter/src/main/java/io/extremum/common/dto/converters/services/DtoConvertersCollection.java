package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.*;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
@Service
public class DtoConvertersCollection implements DtoConverters {
    private final List<FromRequestDtoConverter<?, ?>> fromRequestConverters;
    private final List<ToRequestDtoConverter<?, ?>> toRequestConverters;
    private final List<ToResponseDtoConverter<?, ?>> toResponseConverters;
    private final List<ReactiveToResponseDtoConverter<?, ?>> reactiveToResponseDtoConverters;
    private final List<ReactiveToRequestDtoConverter<?, ?>> reactiveToRequestDtoConverters;

    @Override
    public <M extends Model, D extends RequestDto> Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(
            Class<? extends M> modelClass) {
        return findConverter(modelClass, fromRequestConverters)
                .map(converter -> (FromRequestDtoConverter<M, D>) converter);
    }

    @Override
    public <M extends Model, D extends RequestDto> Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(
            Class<? extends M> modelClass) {
        return findConverter(modelClass, toRequestConverters)
                .map(converter -> (ToRequestDtoConverter<M, D>) converter);
    }

    @Override
    public <M extends Model, D extends ResponseDto> Optional<ToResponseDtoConverter<M, D>> findToResponseDtoConverter(
            Class<? extends M> modelClass) {
        return findConverter(modelClass, toResponseConverters)
                .map(converter -> (ToResponseDtoConverter<M, D>) converter);
    }

    @Override
    public <M extends Model, D extends ResponseDto> Optional<ReactiveToResponseDtoConverter<M, D>> findReactiveToResponseDtoConverter(Class<? extends M> modelClass) {
        return findConverter(modelClass, reactiveToResponseDtoConverters)
                .map(converter -> (ReactiveToResponseDtoConverter<M, D>) converter);
    }

    @Override
    public <M extends Model, D extends RequestDto> Optional<ReactiveToRequestDtoConverter<M, D>> findReactiveToRequestDtoConverter(Class<? extends M> modelClass) {
        return findConverter(modelClass, reactiveToRequestDtoConverters)
                .map(converter -> (ReactiveToRequestDtoConverter<M, D>) converter);
    }

    private <T extends DtoConverter> Optional<T> findConverter(Class<? extends Model> modelClass, List<T> converters) {
        if (!ModelUtils.hasModelName(modelClass)) {
            return Optional.empty();
        }

        String modelName = ModelUtils.getModelName(modelClass);

        for (T converter : converters) {
            if (modelName.equals(converter.getSupportedModel())) {
                return Optional.of(converter);
            }
        }

        return Optional.empty();
    }
}
