package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface EverythingCollectionService {
    CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id, Projection projection, boolean expand);

    Mono<CollectionFragment<ResponseDto>> fetchCollectionReactively(CollectionDescriptor id,
                                                                    Projection projection, boolean expand);

    Flux<ResponseDto> streamCollection(CollectionDescriptor id, Projection projection, boolean expand);
}
