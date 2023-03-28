package io.extremum.everything.services.management;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EverythingCollectionManagementService {
    Response fetchCollection(Descriptor collectionId, Projection projection, boolean expand);

    Mono<Response> fetchCollectionReactively(Descriptor collectionId, Projection projection, boolean expand);

    Flux<ResponseDto> streamCollection(String collectionId, Projection projection, boolean expand);

    void removeFromCollection(Descriptor collectionId, Descriptor nestedId);
}
