package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import reactor.core.publisher.Mono;

public interface ReactiveToRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    Mono<D> convertToRequestReactively(M model, ConversionConfig config);

    Class<? extends D> getRequestDtoType();
}
