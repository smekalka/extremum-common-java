package io.extremum.common.service;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveCommonService<M extends Model> {
    Mono<M> get(String id);

    Mono<M> delete(String id);

    Mono<M> save(M data);

    Mono<M> save(M nested, Model folder);

    Mono<M> create(M data);

    Flux<M> create(List<M> data);
}
