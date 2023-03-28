package io.extremum.descriptors.sync.dao.impl;

import io.extremum.descriptors.common.dao.DescriptorIriMapLoader;
import io.extremum.descriptors.common.redisson.CompositeCodecWithQuickFix;
import io.extremum.descriptors.common.redisson.FlexibleLocalCachedMapOptions;
import io.extremum.descriptors.common.dao.DescriptorCodecs;
import io.extremum.descriptors.common.dao.DescriptorCoordinatesMapLoader;
import io.extremum.descriptors.common.dao.DescriptorIdMapLoader;
import io.extremum.descriptors.common.dao.DescriptorInternalIdMapLoader;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.Descriptor.Readiness;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.cache.CacheValue;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.concurrent.TimeUnit;

public class BaseDescriptorDaoImpl extends BaseDescriptorDao {
    public BaseDescriptorDaoImpl(RedissonClient redissonClient, DescriptorRepository descriptorRepository,
                                 MongoOperations descriptorMongoOperations,
                                 String descriptorsMapName, String internalIdsMapName,
                                 String collectionCoordinatesMapName,
                                 String irisMapName,
                                 int cacheSize, long idleTime) {
        super(
                descriptorMongoOperations,
                redissonClient.getLocalCachedMap(
                        descriptorsMapName,
                        new CompositeCodecWithQuickFix(new StringCodec(), DescriptorCodecs.codecForDescriptor()),
                        FlexibleLocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .shouldBeCached(BaseDescriptorDaoImpl::descriptorWillNeverChange)
                                .loader(new DescriptorIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                ),

                redissonClient.getLocalCachedMap(
                        internalIdsMapName,
                        stringToStringCodec(),
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorInternalIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                ),

                redissonClient.getLocalCachedMap(
                        collectionCoordinatesMapName,
                        stringToStringCodec(),
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorCoordinatesMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                ),

                redissonClient.getLocalCachedMap(
                        irisMapName,
                        stringToStringCodec(),
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorIriMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                )
        );
    }

    private static Codec stringToStringCodec() {
        StringCodec stringCodec = new StringCodec();
        return new CompositeCodec(stringCodec, stringCodec);
    }

    private static boolean descriptorWillNeverChange(CacheValue cacheValue) {
        return !isBlankDescriptor(cacheValue);
    }

    private static boolean isBlankDescriptor(CacheValue cacheValue) {
        if (cacheValue == null) {
            return false;
        }

        Object object = cacheValue.getValue();
        if (!(object instanceof Descriptor)) {
            return false;
        }

        Descriptor descriptor = (Descriptor) object;
        return descriptor.getReadiness() == Readiness.BLANK;
    }
}
