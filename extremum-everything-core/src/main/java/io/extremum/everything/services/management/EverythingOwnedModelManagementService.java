package io.extremum.everything.services.management;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import reactor.core.publisher.Mono;

public interface EverythingOwnedModelManagementService {

    Mono<Response> fetchOwnedModelReactively(Descriptor id, boolean expand);
}