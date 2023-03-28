package io.extremum.common.descriptor.resolve;

import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface ResponseDtoDescriptorResolver {
    Mono<Void> resolveExternalIdsIn(ResponseDto responseDto);
}
