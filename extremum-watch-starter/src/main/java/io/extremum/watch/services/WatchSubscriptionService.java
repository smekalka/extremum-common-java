package io.extremum.watch.services;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;

public interface WatchSubscriptionService {
    void subscribe(Collection<Descriptor> ids, String subscriber);

    void unsubscribe(Collection<Descriptor> ids, String subscriber);

    Collection<String> findAllSubscribersBySubscription(String subscriptionId);

    Boolean isFreshSubscription(String modelId, String subscriberId);
}
