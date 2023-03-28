package io.extremum.descriptors.reactive.dao.impl;

import io.extremum.descriptors.common.dao.*;
import io.extremum.descriptors.common.redisson.CompositeCodecWithQuickFix;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

import java.util.concurrent.TimeUnit;

public class BaseReactiveDescriptorDaoImpl extends BaseReactiveDescriptorDao {
    public BaseReactiveDescriptorDaoImpl(RedissonReactiveClient redissonClient,
                                         DescriptorRepository descriptorRepository,
                                         ReactiveMongoOperations reactiveMongoOperations,
                                         ReactiveMongoDatabaseFactory mongoDatabaseFactory,
                                         String descriptorsMapName, String internalIdsMapName,
                                         String collectionCoordinatesMapName,
                                         String irisMapName,
                                         String ownedCoordinatesMapName,
                                         int cacheSize, long idleTime) {
        super(
                redissonClient.getMap(
                        descriptorsMapName,
                        new CompositeCodecWithQuickFix(new StringCodec(), DescriptorCodecs.codecForDescriptor()),
                        LocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .loader(new DescriptorIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                ),

                redissonClient.getMap(
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

                redissonClient.getMap(
                        collectionCoordinatesMapName,
                        stringToStringCodec(),
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorCoordinatesMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                redissonClient.getMap(
                        irisMapName,
                        stringToStringCodec(),
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorIriMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                redissonClient.getMap(
                        irisMapName,
                        stringToStringCodec(),
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new OwnedDescriptorCoordinatesMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                reactiveMongoOperations, mongoDatabaseFactory);
    }

    private static Codec stringToStringCodec() {
        StringCodec stringCodec = new StringCodec();
        return new CompositeCodec(stringCodec, stringCodec);
    }
}
