package io.extremum.watch.services;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ReactiveWatchSubscriptionService {
    Mono<Void> subscribe(Collection<Descriptor> ids, String subscriber);

    Mono<Void> unsubscribe(Collection<Descriptor> ids, String subscriber);

    Mono<Collection<String>> findAllSubscribersBySubscription(String subscriptionId);

    Mono<Boolean> isFreshSubscription(String modelId, String subscriberId);
}

