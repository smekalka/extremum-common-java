package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import reactor.core.publisher.Mono;

public interface CollectionMakeup {
    void applyCollectionMakeup(ResponseDto rootDto);

    Mono<Void> applyCollectionMakeupReactively(ResponseDto rootDto);

    Mono<Void> applyCollectionMakeupReactively(CollectionReference<?> collectionReference,
                                               CollectionDescriptor collectionDescriptor, ResponseDto rootDto);
}