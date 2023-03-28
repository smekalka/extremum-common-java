package io.extremum.dynamic.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface DynamicModelRemoveStrategy {
    Mono<Void> remove(Descriptor id, String collectionName);
}
