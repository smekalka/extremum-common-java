package io.extremum.watch.repositories.impl;

import io.extremum.watch.config.WatchProperties;
import io.extremum.watch.repositories.SubscriptionRepository;
import org.redisson.api.RSetCache;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static io.extremum.watch.repositories.impl.RedisSubscriptionRepositoryUtils.FRESH_SUBSCRIPTION_SET;
import static io.extremum.watch.repositories.impl.RedisSubscriptionRepositoryUtils.makeFreshSetItem;

@Repository
public class RedisSubscriptionRepository implements SubscriptionRepository {
    private final RedissonClient client;
    private final WatchProperties properties;
    private final RSetCache<String> freshSubscriptions;


    public RedisSubscriptionRepository(RedissonClient client, WatchProperties properties) {
        this.client = client;
        this.properties = properties;
        this.freshSubscriptions = client.getSetCache(FRESH_SUBSCRIPTION_SET);
    }

    @Override
    public void subscribe(Collection<String> modelIds, String subscriberId) {
        modelIds.forEach(modelId -> subscribeToOne(modelId, subscriberId));
    }

    private void subscribeToOne(String modelId, String subscriberId) {
        RSetCache<String> subscribers = subscriptionSet(modelId);
        subscribers.add(subscriberId, properties.getSubscriptionTimeToLiveDays(), TimeUnit.DAYS);
        freshSubscriptions.add(makeFreshSetItem(modelId, subscriberId),
                properties.getSubscriptionTimeToLiveDays(),
                TimeUnit.DAYS);
    }

    private RSetCache<String> subscriptionSet(String modelId) {
        return client.getSetCache(subscriptionKey(modelId));
    }

    private String subscriptionKey(String modelId) {
        return "watch-subscription:" + modelId;
    }

    @Override
    public void unsubscribe(Collection<String> modelIds, String subscriberId) {
        modelIds.forEach(modelId -> unsubscribeFromOne(modelId, subscriberId));
    }

    private void unsubscribeFromOne(String modelId, String subscriberId) {
        RSetCache<String> subscribers = subscriptionSet(modelId);
        subscribers.remove(subscriberId);
        freshSubscriptions.remove(makeFreshSetItem(modelId, subscriberId));
    }

    @Override
    public Collection<String> getAllSubscribersIdsBySubscription(String modelId) {
        return new ArrayList<>(subscriptionSet(modelId));
    }

    @Override
    public boolean checkFreshSubscription(String modelId, String subscriberId) {
        return freshSubscriptions.remove(makeFreshSetItem(modelId, subscriberId));
    }
}
