package io.extremum.dynamic.dao;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface DynamicModelDao<T extends DynamicModel<?>> {
    Mono<T> create(T model, String collectionName);

    Mono<T> update(T model, String collectionName);

    Mono<T> getByIdFromCollection(Descriptor id, String collectionName);

    Mono<Void> remove(Descriptor id, String collectionName);
}
