package io.extremum.descriptors.reactive.dao;

import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.reactive.dao.impl.BaseReactiveDescriptorDaoImpl;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.reactive.dao.impl.JpaReactiveDescriptorDao;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

public class ReactiveDescriptorDaoFactory {
    public static ReactiveDescriptorDao create(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonReactiveClient redissonClient, DescriptorRepository descriptorRepository,
            ReactiveMongoOperations reactiveMongoOperations,
            ReactiveMongoDatabaseFactory mongoDatabaseFactory) {
        return new BaseReactiveDescriptorDaoImpl(redissonClient, descriptorRepository,
                reactiveMongoOperations, mongoDatabaseFactory,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                descriptorsProperties.getIriMapName(),
                descriptorsProperties.getOwnedCoordinatesMapName(),
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    private ReactiveDescriptorDaoFactory() {}

    public static ReactiveDescriptorDao create(DescriptorRepository descriptorRepository){
        return new JpaReactiveDescriptorDao(descriptorRepository);
    }
}
