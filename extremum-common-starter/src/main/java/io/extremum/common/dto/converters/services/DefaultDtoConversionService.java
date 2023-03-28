package io.extremum.common.dto.converters.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.dto.converters.*;
import io.extremum.common.exceptions.ConverterNotFoundException;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;

@RequiredArgsConstructor
@Service
public class DefaultDtoConversionService implements DtoConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDtoConversionService.class);

    private final DtoConverters dtoConverters;
    private final StubDtoConverter stubDtoConverter;
    private final ObjectMapper mapper;

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        ToResponseDtoConverter<Model, ResponseDto> converter = findToResponseConverter(model);
        return converter.convertToResponse(model, config);
    }

    private ToResponseDtoConverter<Model, ResponseDto> findToResponseConverter(Model model) {
        return dtoConverters.<Model, ResponseDto>findToResponseDtoConverter(model.getClass())
                    .orElseGet(() -> warnAndGetStubConverter(model));
    }

    @Override
    public Mono<ResponseDto> convertUnknownToResponseDtoReactively(Model model, ConversionConfig config) {
        return Mono.fromSupplier(() -> findReactiveToResponseConverter(model))
                .flatMap(converter -> converter.convertToResponseReactively(model, config));
    }

    private ReactiveToResponseDtoConverter<Model, ResponseDto> findReactiveToResponseConverter(Model model) {
        return dtoConverters.<Model, ResponseDto>findReactiveToResponseDtoConverter(model.getClass())
                .orElseGet(() -> warnAndGetReactiveStubConverter(model));
    }

    private ToResponseDtoConverter<Model, ResponseDto> warnAndGetStubConverter(Model model) {
        LOGGER.error("Unable to find a to-response-dto-converter for model {}: {}",
                model.getClass().getSimpleName(), model);
        return this.stubDtoConverter;
    }

    private ReactiveToResponseDtoConverter<Model, ResponseDto> warnAndGetReactiveStubConverter(Model model) {
        LOGGER.error("Unable to find a reactive to-response-dto-converter for model {}: {}",
                model.getClass().getSimpleName(), model);
        return this.stubDtoConverter;
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        ToRequestDtoConverter<Model, RequestDto> converter = findMandatoryToRequestConverter(model.getClass());
        return converter.convertToRequest(model, config);
    }

    @Override
    public <M extends Model, D extends RequestDto> M convertFromRequestDto(Map<String, Object> values, Class<M> modelClass) {
        return mapper.convertValue(values, modelClass);
    }

    @Override
    public Mono<RequestDto> convertUnknownToRequestDtoReactively(Model model, ConversionConfig config) {
        return Mono.fromSupplier(() -> findMandatoryReactiveToRequestConverter(model.getClass()))
                .flatMap(converter -> converter.convertToRequestReactively(model, config));
    }

    @Override
    public <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<M> modelClass, D dto) {
        FromRequestDtoConverter<M, D> converter = dtoConverters.<M, D>findFromRequestDtoConverter(modelClass)
                .orElseThrow(onConverterNotFound("from-request", modelClass));
        return converter.convertFromRequest(dto);
    }

    private Supplier<ConverterNotFoundException> onConverterNotFound(String type, Class<? extends Model> modelClass) {
        return () -> new ConverterNotFoundException(
                format("Unable to find %s for model '%s'", type, modelClass.getSimpleName()));
    }

    private ToRequestDtoConverter<Model, RequestDto> findMandatoryToRequestConverter(
            Class<? extends Model> modelClass) {
        return dtoConverters.<Model, RequestDto>findToRequestDtoConverter(modelClass)
                    .orElseThrow(onConverterNotFound("to-request", modelClass));
    }

    private ReactiveToRequestDtoConverter<Model, RequestDto> findMandatoryReactiveToRequestConverter(
            Class<? extends Model> modelClass) {
        return dtoConverters.<Model, RequestDto>findReactiveToRequestDtoConverter(modelClass)
                    .orElseThrow(onConverterNotFound("reactive to-request", modelClass));
    }

    @Override
    public Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass) {
        return findMandatoryToRequestConverter(modelClass).getRequestDtoType();
    }

    @Override
    public Class<? extends RequestDto> findReactiveRequestDtoType(Class<? extends Model> modelClass) {
        return findMandatoryReactiveToRequestConverter(modelClass).getRequestDtoType();
    }
}
