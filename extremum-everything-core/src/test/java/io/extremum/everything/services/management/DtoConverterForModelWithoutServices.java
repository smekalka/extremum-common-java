package io.extremum.everything.services.management;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ToRequestDtoConverter;

/**
 * @author rpuch
 */
class DtoConverterForModelWithoutServices
        implements ToRequestDtoConverter<MongoModelWithoutServices, RequestDtoForModelWithoutServices>,
        FromRequestDtoConverter<MongoModelWithoutServices, RequestDtoForModelWithoutServices> {
    @Override
    public RequestDtoForModelWithoutServices convertToRequest(MongoModelWithoutServices model, ConversionConfig config) {
        return new RequestDtoForModelWithoutServices();
    }

    @Override
    public MongoModelWithoutServices convertFromRequest(RequestDtoForModelWithoutServices dto) {
        return new MongoModelWithoutServices();
    }

    @Override
    public Class<RequestDtoForModelWithoutServices> getRequestDtoType() {
        return RequestDtoForModelWithoutServices.class;
    }

    @Override
    public String getSupportedModel() {
        return MongoModelWithoutServices.class.getSimpleName();
    }
}
