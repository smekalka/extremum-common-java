package io.extremum.everything.services.management;

import io.extremum.sharedmodels.fundamental.CommonResponseDto;

/**
 * @author rpuch
 */
class ResponseDtoForModelWithoutServices extends CommonResponseDto {
    @Override
    public String getModel() {
        return "WithoutServices";
    }
}
