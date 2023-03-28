package io.extremum.watch.repositories;

import reactor.core.publisher.Mono;

import java.util.Collection;

public interface SubscriptionRepository {
    void subscribe(Collection<String> modelIds, String subscriberId);

    void unsubscribe(Collection<String> modelIds, String subscriberId);

    Collection<String> getAllSubscribersIdsBySubscription(String modelId);

    boolean checkFreshSubscription(String modelId, String subscriberId);
}
