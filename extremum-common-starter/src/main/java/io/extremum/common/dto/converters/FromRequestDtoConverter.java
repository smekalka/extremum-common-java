package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.basic.Model;

public interface FromRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    M convertFromRequest(D dto);

    Class<? extends D> getRequestDtoType();
}
