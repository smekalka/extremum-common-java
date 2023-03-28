package io.extremum.watch.repositories;

import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ReactiveSubscriptionRepository {
    Mono<Void> subscribe(Collection<String> modelIds, String subscriberId);

    Mono<Void> unsubscribe(Collection<String> modelIds, String subscriberId);

    Mono<Collection<String>> getAllSubscribersIdsBySubscription(String modelId);

    Mono<Boolean> checkFreshSubscription(String modelId, String subscriberId);
}
