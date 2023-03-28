package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.test.TestWithServices;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@SpringBootTest(classes = DescriptorConfiguration.class)
class ReactiveDescriptorRedisSerializationTest extends TestWithServices {
    @Autowired
    private ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;
    @Autowired
    private ReactiveDescriptorSaver descriptorSaver;

    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    void storesDescriptorsMapKeyInRedisAsPlainString() {
        Descriptor descriptor = createADescriptor();
        RMap<String, String> descriptorsMap = redissonClient.getMap(descriptorsProperties.getDescriptorsMapName(),
                new StringCodec());

        assertThat(descriptorsMap.get(descriptor.getExternalId()), is(notNullValue()));
    }

    private Descriptor createADescriptor() {
        return mongoDescriptorFacilities.createOrGet(new ObjectId().toString(), "test_model", "iri").block();
    }

    @Test
    void storesInternalIdsMapKeyAndValueInRedisAsPlainStrings() {
        Descriptor descriptor = createADescriptor();
        RMap<String, String> internalIdsMap = redissonClient.getMap(descriptorsProperties.getInternalIdsMapName(),
                new StringCodec());

        assertThat(internalIdsMap.get(descriptor.getInternalId()), is(descriptor.getExternalId()));
    }

    @Test
    void storesCollectionCoordinatesMapKeyAndValueInRedisAsPlainStrings() {
        RMap<String, String> collectionCoordinatesMap = redissonClient.getMap(
                descriptorsProperties.getCollectionCoordinatesMapName(), new StringCodec());

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree(randomString());
        Descriptor savedDescriptor = descriptorSaver.createAndSave(collectionDescriptor).block();

        assertThat(collectionCoordinatesMap.get(collectionDescriptor.toCoordinatesString()),
                is(savedDescriptor.getExternalId()));
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }
}
