package io.extremum.descriptors.common.dao;

import org.redisson.api.map.MapLoader;

public abstract class CarefulMapLoader<K, V> implements MapLoader<K, V> {
    @Override
    public final Iterable<K> loadAllKeys() {
        throw new UnsupportedOperationException("We do not allow to load all keys/values");
    }
}
