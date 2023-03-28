package io.extremum.common.collection.visit;

import io.extremum.sharedmodels.dto.ResponseDto;

public interface CollectionVisitDriver {
    void visitCollectionsInResponseDto(ResponseDto dto);
}
