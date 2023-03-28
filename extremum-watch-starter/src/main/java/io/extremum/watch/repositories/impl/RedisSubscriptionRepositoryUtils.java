package io.extremum.watch.repositories.impl;

public class RedisSubscriptionRepositoryUtils {
    public static final String FRESH_SUBSCRIPTION_SET = "watch-fresh-subscriptions";

    public static String makeFreshSetItem(String modelId, String subscriberId) {
        return modelId + ':' + subscriberId;
    }
}
