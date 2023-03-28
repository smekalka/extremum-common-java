package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface ReactiveToResponseDtoConverter<M extends Model, D extends ResponseDto> extends DtoConverter {
    Mono<D> convertToResponseReactively(M model, ConversionConfig config);

    default Mono<D> convertToResponseReactively(M model) {
        return convertToResponseReactively(model, ConversionConfig.defaults());
    }

    Class<? extends D> getResponseDtoType();
}
