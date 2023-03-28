package io.extremum.everything.services.management;

import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;

public interface CreateManagementService {
    ResponseDto create(String modelName, RequestDto requestDto, boolean expand);
}