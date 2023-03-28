package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.ResponseDto;

public interface ToResponseDtoConverter<M extends Model, D extends ResponseDto> extends DtoConverter {
    D convertToResponse(M model, ConversionConfig config);

    default D convertToResponse(M model) {
        return convertToResponse(model, ConversionConfig.defaults());
    }

    Class<? extends D> getResponseDtoType();
}
