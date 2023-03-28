package io.extremum.everything.services.management;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ToRequestDtoConverter;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

/**
 * @author rpuch
 */
class MockDtoConversionService implements DtoConversionService {

    private <M extends Model, D extends RequestDto> Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(
            Class<? extends M> modelClass) {
        if (modelClass == MongoModelWithServices.class) {
            return Optional.of((FromRequestDtoConverter<M, D>) new DtoConverterForModelWithServices());
        }
        if (modelClass == MongoModelWithoutServices.class) {
            return Optional.of((FromRequestDtoConverter<M, D>) new DtoConverterForModelWithoutServices());
        }
        throw new UnsupportedOperationException();
    }

    private <M extends Model, D extends RequestDto> Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(
            Class<? extends M> modelClass) {
        if (modelClass == MongoModelWithServices.class) {
            return Optional.of((ToRequestDtoConverter<M, D>) new DtoConverterForModelWithServices());
        }
        if (modelClass == MongoModelWithoutServices.class) {
            return Optional.of((ToRequestDtoConverter<M, D>) new DtoConverterForModelWithoutServices());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        if (model instanceof MongoModelWithServices) {
            return new ResponseDtoForModelWithServices();
        }
        if (model instanceof MongoModelWithoutServices) {
            return new ResponseDtoForModelWithoutServices();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<ResponseDto> convertUnknownToResponseDtoReactively(Model model, ConversionConfig config) {
        return Mono.just(convertUnknownToResponseDto(model, config));
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        if (model instanceof MongoModelWithServices) {
            return new RequestDtoForModelWithServices();
        }
        if (model instanceof MongoModelWithoutServices) {
            return new RequestDtoForModelWithoutServices();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<RequestDto> convertUnknownToRequestDtoReactively(Model model, ConversionConfig config) {
        return Mono.just(convertUnknownToRequestDto(model, config));
    }

    @Override
    public <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<M> modelClass, D dto) {
        return findFromRequestDtoConverter(modelClass)
                .orElseThrow(() -> new IllegalArgumentException("No from request dto converter for " + modelClass))
                .convertFromRequest(dto);
    }

    @Override
    public Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass) {
        return findToRequestDtoConverter(modelClass)
                .orElseThrow(() -> new IllegalStateException("No to-request-converter to " + modelClass))
                .getRequestDtoType();
    }

    @Override
    public Class<? extends RequestDto> findReactiveRequestDtoType(Class<? extends Model> modelClass) {
        return findRequestDtoType(modelClass);
    }

    @Override
    public <M extends Model, D extends RequestDto> M convertFromRequestDto(Map<String, Object> values, Class<M> modelClass) {
        return null;
    }
}
