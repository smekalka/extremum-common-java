package io.extremum.descriptors.sync.dao;

import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.sync.dao.impl.BaseDescriptorDaoImpl;
import io.extremum.descriptors.sync.dao.impl.InMemoryDescriptorDao;
import io.extremum.descriptors.sync.dao.impl.JpaDescriptorDaoImpl;
import io.extremum.descriptors.sync.dao.impl.JpaDescriptorRepository;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoOperations;

public class DescriptorDaoFactory {
    public static DescriptorDao createBaseDescriptorDao(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonClient redissonClient, DescriptorRepository descriptorRepository,
            MongoOperations descriptorMongoOperations) {
        return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository, descriptorMongoOperations,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                descriptorsProperties.getIriMapName(),
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    public static DescriptorDao createInMemoryDescriptorDao() {
        return new InMemoryDescriptorDao();
    }

    public static DescriptorDao createJpaDescriptorDao(RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
                                                       RedissonClient redissonClient, DescriptorRepository descriptorRepository,
                                                       JpaDescriptorRepository jpaDescriptorRepository) {
        return new JpaDescriptorDaoImpl(redissonClient, descriptorRepository, jpaDescriptorRepository,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                descriptorsProperties.getIriMapName(),
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    private DescriptorDaoFactory() {
    }
}
