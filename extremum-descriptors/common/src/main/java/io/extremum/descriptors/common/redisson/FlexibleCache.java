package io.extremum.descriptors.common.redisson;

import org.redisson.cache.Cache;
import org.redisson.cache.CacheKey;
import org.redisson.cache.CacheValue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

class FlexibleCache implements Cache<CacheKey, CacheValue> {
    private final ConcurrentMap<CacheKey, CacheValue> delegate;
    private final Predicate<CacheValue> shouldBeCached;

    public FlexibleCache(ConcurrentMap<CacheKey, CacheValue> delegate) {
        this(delegate, value -> true);
    }

    public FlexibleCache(ConcurrentMap<CacheKey, CacheValue> delegate, Predicate<CacheValue> shouldBeCached) {
        this.delegate = delegate;
        this.shouldBeCached = shouldBeCached;
    }

    @Override
    public CacheValue putIfAbsent(CacheKey key, CacheValue value) {
        if (shouldBeCached(value)) {
            return delegate.putIfAbsent(key, value);
        }
        return null;
    }

    private boolean shouldBeCached(CacheValue value) {
        return shouldBeCached.test(value);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public CacheValue get(Object key) {
        return delegate.get(key);
    }

    @Override
    public CacheValue put(CacheKey key, CacheValue value) {
        if (shouldBeCached(value)) {
            return delegate.put(key, value);
        }
        return null;
    }

    @Override
    public CacheValue remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends CacheKey, ? extends CacheValue> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<CacheKey> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<CacheValue> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<CacheKey, CacheValue>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean remove(Object key, Object value) {
        return delegate.remove(key, value);
    }

    @Override
    public boolean replace(CacheKey key, CacheValue oldValue, CacheValue newValue) {
        if (shouldBeCached(newValue)) {
            return delegate.replace(key, oldValue, newValue);
        }
        return false;
    }

    @Override
    public CacheValue replace(CacheKey key, CacheValue value) {
        if (shouldBeCached(value)) {
            return delegate.replace(key, value);
        }
        return null;
    }
}
