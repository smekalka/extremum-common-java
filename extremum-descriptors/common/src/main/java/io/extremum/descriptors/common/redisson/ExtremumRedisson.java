package io.extremum.descriptors.common.redisson;

import org.redisson.Redisson;
import org.redisson.WriteBehindService;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

public class ExtremumRedisson extends Redisson {

    public ExtremumRedisson(Config redissonConfig) {
        super(redissonConfig);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec,
                                                          LocalCachedMapOptions<K, V> options) {
        return new ExtremumRedissonLocalCachedMap<>(codec, commandExecutor, name, options, evictionScheduler, this, new WriteBehindService(commandExecutor));
    }
}
