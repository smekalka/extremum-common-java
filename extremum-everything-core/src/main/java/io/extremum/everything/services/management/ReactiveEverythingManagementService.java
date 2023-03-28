package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface ReactiveEverythingManagementService {
    Mono<ResponseDto> get(Descriptor id, boolean expand);

    Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand);

    Mono<Void> remove(Descriptor id);
}
