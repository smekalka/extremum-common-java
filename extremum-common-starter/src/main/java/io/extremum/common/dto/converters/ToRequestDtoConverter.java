package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.basic.Model;

public interface ToRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    D convertToRequest(M model, ConversionConfig config);

    Class<? extends D> getRequestDtoType();
}
