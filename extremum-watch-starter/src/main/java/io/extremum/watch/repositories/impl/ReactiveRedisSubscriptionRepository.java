package io.extremum.watch.repositories.impl;

import io.extremum.watch.config.WatchProperties;
import io.extremum.watch.repositories.ReactiveSubscriptionRepository;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.extremum.watch.repositories.impl.RedisSubscriptionRepositoryUtils.FRESH_SUBSCRIPTION_SET;
import static io.extremum.watch.repositories.impl.RedisSubscriptionRepositoryUtils.makeFreshSetItem;

@Repository
public class ReactiveRedisSubscriptionRepository implements ReactiveSubscriptionRepository {
    private final RedissonReactiveClient client;
    private final WatchProperties properties;
    private final RSetCacheReactive<String> freshSubscriptions;

    public ReactiveRedisSubscriptionRepository(RedissonReactiveClient client, WatchProperties properties) {
        this.client = client;
        this.properties = properties;
        this.freshSubscriptions = client.getSetCache(FRESH_SUBSCRIPTION_SET);
    }

    @Override
    public Mono<Void> subscribe(Collection<String> modelIds, String subscriberId) {
        return Flux.fromIterable(modelIds)
                .flatMap(modelId -> subscribeToOne(modelId, subscriberId))
                .then();
    }

    private Mono<Void> subscribeToOne(String modelId, String subscriberId) {
        RSetCacheReactive<String> subscribers = subscriptionSet(modelId);
        return subscribers.add(subscriberId, properties.getSubscriptionTimeToLiveDays(), TimeUnit.DAYS)
                .and(freshSubscriptions.add(makeFreshSetItem(modelId, subscriberId),
                        properties.getSubscriptionTimeToLiveDays(),
                        TimeUnit.DAYS));
    }

    private RSetCacheReactive<String> subscriptionSet(String modelId) {
        return client.getSetCache(subscriptionKey(modelId));
    }

    private String subscriptionKey(String modelId) {
        return "watch-subscription:" + modelId;
    }

    @Override
    public Mono<Void> unsubscribe(Collection<String> modelIds, String subscriberId) {
        return Flux.fromIterable(modelIds)
                .flatMap(modelId -> unsubscribeFromOne(modelId, subscriberId))
                .then();
    }

    private Mono<Void> unsubscribeFromOne(String modelId, String subscriberId) {
        RSetCacheReactive<String> subscribers = subscriptionSet(modelId);
        return subscribers.remove(subscriberId)
                .and(freshSubscriptions.remove(makeFreshSetItem(modelId, subscriberId)));
    }

    @Override
    public Mono<Collection<String>> getAllSubscribersIdsBySubscription(String modelId) {
        return subscriptionSet(modelId).readAll().map(ArrayList::new);
    }

    @Override
    public Mono<Boolean> checkFreshSubscription(String modelId, String subscriberId) {
        return freshSubscriptions.remove(makeFreshSetItem(modelId, subscriberId));
    }
}
