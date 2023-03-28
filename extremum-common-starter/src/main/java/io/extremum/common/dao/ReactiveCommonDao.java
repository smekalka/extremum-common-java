package io.extremum.common.dao;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveCommonDao<M, ID> {
    Flux<M> findAll();

    Mono<M> findById(ID id);

    Mono<Boolean> existsById(ID id);

    <N extends M> Mono<N> save(N model);

    <N extends M> Flux<N> saveAll(Iterable<N> entities);

    Mono<Void> deleteById(ID id);

    Mono<M> deleteByIdAndReturn(ID id);
}
