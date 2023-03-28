package io.extremum.everything.services.management;

import reactor.core.publisher.Mono;

public interface ReactiveRemover {
    Mono<Void> remove(String id);
}
