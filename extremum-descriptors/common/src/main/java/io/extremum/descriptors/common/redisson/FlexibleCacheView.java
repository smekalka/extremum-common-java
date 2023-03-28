package io.extremum.descriptors.common.redisson;

import org.redisson.RedissonObject;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.cache.CacheKey;
import org.redisson.cache.CacheValue;
import org.redisson.cache.LocalCacheView;

import java.util.concurrent.ConcurrentMap;

public class FlexibleCacheView<K, V> extends LocalCacheView<K, V> {

    public FlexibleCacheView(LocalCachedMapOptions<?, ?> options, RedissonObject object) {
        super(options, object);
    }

    @Override
    public ConcurrentMap<CacheKey, CacheValue> createCache(LocalCachedMapOptions<?, ?> options) {
        ConcurrentMap<CacheKey, CacheValue> delegate = super.createCache(options);

        if (options instanceof FlexibleLocalCachedMapOptions) {
            return new FlexibleCache(delegate,
                    ((FlexibleLocalCachedMapOptions<K, V>) options).getShouldBeCached());
        } else {
            return new FlexibleCache(delegate);
        }
    }
}
