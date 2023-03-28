package io.extremum.common.collection.conversion;

import reactor.core.publisher.Mono;

public interface CollectionMakeupModule {
    void applyToCollection(CollectionMakeupRequest request);

    Mono<Void> applyToCollectionReactively(CollectionMakeupRequest request);
}
