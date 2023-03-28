package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.descriptors.sync.dao.DescriptorDaoFactory;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorRedisSerializationTest extends TestWithServices {
    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    private DescriptorRepository descriptorRepository;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;
    @Autowired
    @DescriptorsMongoDb
    private MongoOperations descriptorMongoOperations;
    @Autowired
    private DescriptorSaver descriptorSaver;

    private DescriptorDao freshDaoToAvoidCachingInMemory;

    private Descriptor descriptor;

    @BeforeEach
    void init() {
        descriptor = createADescriptor();

        freshDaoToAvoidCachingInMemory = DescriptorDaoFactory.createBaseDescriptorDao(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository, descriptorMongoOperations);
    }

    @Test
    void whenLoadingADescriptorByExternalIdFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Optional<Descriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByExternalId(
                descriptor.getExternalId());

        assertThatRetrievedDescriptorIsOk(descriptor, retrievedDescriptor);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertThatRetrievedDescriptorIsOk(Descriptor descriptor, Optional<Descriptor> retrievedDescriptor) {
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor.getExternalId(), retrievedDescriptor.get().getExternalId());
        assertEquals(descriptor.getInternalId(), retrievedDescriptor.get().getInternalId());
    }

    @Test
    void whenLoadingADescriptorByInternalIdFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Optional<Descriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByInternalId(
                descriptor.getInternalId());

        assertThatRetrievedDescriptorIsOk(descriptor, retrievedDescriptor);
    }

    private Descriptor createADescriptor() {
        ObjectId objectId = new ObjectId();
        return mongoDescriptorFacilities.create(objectId, "test_model", "iri", new HashMap<>());
    }

    @Test
    void whenLoadingACollectionDescriptorByCoordinatesFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Descriptor hostDescriptor = createADescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostDescriptor, "items");
        Descriptor descriptor = Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
        descriptorService.store(descriptor);

        Descriptor retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByCollectionCoordinates(
                collectionDescriptor.getCoordinatesString()).orElse(null);

        assertThat(retrievedDescriptor, is(notNullValue()));
        assertThatCollectionDescriptorRetrievalWasOk(hostDescriptor.getExternalId(),
                retrievedDescriptor);
    }

    private void assertThatCollectionDescriptorRetrievalWasOk(String hostExternalId,
                                                              Descriptor retrievedDescriptor) {
        assertThat(retrievedDescriptor.getExternalId(), is(notNullValue()));
        assertThat(retrievedDescriptor.getCollection().getType(), is(CollectionDescriptor.Type.OWNED));
        OwnedCoordinates ownedCoordinates = retrievedDescriptor.getCollection().getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates.getHostId().getExternalId(), is(hostExternalId));
        assertThat(ownedCoordinates.getHostAttributeName(), is("items"));
    }

    @Test
    void storesDescriptorsMapKeyInRedisAsPlainString() {
        RMap<String, String> descriptorsMap = redissonClient.getMap(descriptorsProperties.getDescriptorsMapName(),
                new StringCodec());

        assertThat(descriptorsMap.get(descriptor.getExternalId()), is(notNullValue()));
    }

    @Test
    void storesInternalIdsMapKeyAndValueInRedisAsPlainStrings() {
        RMap<String, String> internalIdsMap = redissonClient.getMap(descriptorsProperties.getInternalIdsMapName(),
                new StringCodec());

        assertThat(internalIdsMap.get(descriptor.getInternalId()), is(descriptor.getExternalId()));
    }

    @Test
    void storesCollectionCoordinatesMapKeyAndValueInRedisAsPlainStrings() {
        RMap<String, String> collectionCoordinatesMap = redissonClient.getMap(
                descriptorsProperties.getCollectionCoordinatesMapName(), new StringCodec());

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree(randomString());
        Descriptor savedDescriptor = descriptorSaver.createAndSave(collectionDescriptor);

        assertThat(collectionCoordinatesMap.get(collectionDescriptor.toCoordinatesString()),
                is(savedDescriptor.getExternalId()));
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }
}
