package io.extremum.common.descriptorpool;

import reactor.core.publisher.Mono;

public interface ReactiveSupplier<T> {
    Mono<T> get();
}
