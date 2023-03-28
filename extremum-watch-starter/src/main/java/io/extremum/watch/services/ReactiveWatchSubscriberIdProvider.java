package io.extremum.watch.services;

import reactor.core.publisher.Mono;

public interface ReactiveWatchSubscriberIdProvider {
    Mono<String> getSubscriberId();
}
