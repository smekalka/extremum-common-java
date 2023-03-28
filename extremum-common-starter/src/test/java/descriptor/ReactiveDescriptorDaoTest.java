package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.dao.DescriptorCodecs;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.common.redisson.CompositeCodecWithQuickFix;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDaoFactory;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.data.mongodb.core.query.Criteria.where;


@SpringBootTest(classes = DescriptorConfiguration.class)
class ReactiveDescriptorDaoTest extends TestWithServices {
    @Autowired
    private ReactiveDescriptorDao reactiveDescriptorDao;
    @Autowired
    private DescriptorRepository descriptorRepository;

    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;
    @Autowired
    private DescriptorSaver descriptorSaver;
    @Autowired
    @MainMongoDb
    private ReactiveTransactionManager reactiveTransactionManager;
    @Autowired
    @DescriptorsMongoDb
    private ReactiveMongoOperations descriptorsMongoOperations;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedissonReactiveClient redissonReactiveClient;
    @Autowired
    private ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;

    private RMap<String, Descriptor> descriptorsInRedis;

    @BeforeEach
    void createRedisMap() {
        descriptorsInRedis = redissonClient.getMap(
                descriptorsProperties.getDescriptorsMapName(),
                new CompositeCodecWithQuickFix(new StringCodec(), DescriptorCodecs.codecForDescriptor())
        );
    }

    @Test
    void shouldStoreBothToMongoAndRedis() {
        Descriptor descriptor = descriptorForStorage();

        Mono<Descriptor> mono = reactiveDescriptorDao.store(descriptor);
        Descriptor savedDescriptor = mono.block();

        assertThatDescriptorWasSavedToMongoAndRedis(savedDescriptor);
    }

    private Descriptor descriptorForStorage() {
        return Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .iri("/hier-part")
                .type(Descriptor.Type.SINGLE)
                .storageType(StandardStorageType.MONGO)
                .build();
    }

    private void assertThatDescriptorWasSavedToMongoAndRedis(Descriptor savedDescriptor) {
        assertThat(savedDescriptor, is(notNullValue()));

        Descriptor fromMongo = descriptorRepository.findByExternalId(savedDescriptor.getExternalId())
                .orElse(null);
        assertThat(fromMongo, is(notNullValue()));
        assertThat(fromMongo.getExternalId(), is(savedDescriptor.getExternalId()));
        assertThat(fromMongo.getInternalId(), is(savedDescriptor.getInternalId()));
        assertThat(fromMongo.getStorageType(), is("mongo"));

        Descriptor fromRedis = descriptorsInRedis.get(savedDescriptor.getExternalId());
        assertThat(fromRedis, is(notNullValue()));
        assertThat(fromRedis.getExternalId(), is(savedDescriptor.getExternalId()));
        assertThat(fromRedis.getInternalId(), is(savedDescriptor.getInternalId()));
        assertThat(fromRedis.getStorageType(), is("mongo"));
    }

    @Test
    void shouldStoreBothToMongoAndRedisUnderTransaction() {
        Descriptor descriptor = descriptorForStorage();

        TransactionalOperator txOp = TransactionalOperator.create(reactiveTransactionManager);

        Mono<Descriptor> mono = txOp.transactional(reactiveDescriptorDao.store(descriptor));
        Descriptor savedDescriptor = mono.block();

        assertThatDescriptorWasSavedToMongoAndRedis(savedDescriptor);
    }

    @Test
    void shouldStoreToRedisOnlyAfterTransactionCommits() {
        Descriptor descriptor = descriptorForStorage();

        TransactionalOperator txOp = TransactionalOperator.create(reactiveTransactionManager);

        Mono<Descriptor> storeAndAssertNoDescriptorSavedToRedis = reactiveDescriptorDao.store(descriptor)
                .doOnNext(savedDescriptor -> {
                    assertThatDescriptorIsNotSavedToRedis(savedDescriptor.getExternalId());
                });
        Mono<Descriptor> mono = txOp.transactional(storeAndAssertNoDescriptorSavedToRedis);
        mono.block();
    }

    private void assertThatDescriptorIsNotSavedToRedis(String externalId) {
        Descriptor fromRedis = descriptorsInRedis.get(externalId);
        assertThat(fromRedis, is(nullValue()));
    }

    @Test
    void testRetrieveByExternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor originalDescriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        String externalId = originalDescriptor.getExternalId();
        assertNotNull(externalId);

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByExternalId(externalId);
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }

    @Test
    void testRetrieveByInternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor originalDescriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        String externalId = originalDescriptor.getExternalId();
        assertNotNull(externalId);

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByInternalId(objectId.toString());
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }

    @Test
    void testRetrieveByCoordinatesString() {
        ObjectId objectId = new ObjectId();
        Descriptor hostDescriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        String hostId = hostDescriptor.getExternalId();
        assertNotNull(hostId);

        Descriptor collectionDescriptor = descriptorSaver.createAndSave(
                CollectionDescriptor.forOwned(hostDescriptor, "items"));

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByCollectionCoordinates(
                collectionDescriptor.getCollection().toCoordinatesString());
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(collectionDescriptor.getExternalId())));
    }

    @Test
    void testRetrieveFromMongo() {
        String internalId = new ObjectId().toString();
        Descriptor originalDescriptor = Descriptor.builder()
                .externalId(createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .build();

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByInternalId(internalId);
        assertThat(mono.block(), is(nullValue()));

        descriptorsMongoOperations.save(originalDescriptor).block();
        mono = reactiveDescriptorDao.retrieveByInternalId(internalId);
        Descriptor foundDescriptor = mono.block();

        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }

    @NotNull
    private String createExternalId() {
        return descriptorService.createExternalId();
    }

    @Test
    void givenADescriptorWithAnInternalIdAlreadyExists_whenSavingAnotherDescriptorWithTheSameInternalId_thenAnExceptionShouldBeThrown() {
        ObjectId objectId = new ObjectId();
        mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        try {
            mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());
            fail("An exception should be thrown");
        } catch (DuplicateKeyException e) {
            assertThat(e.getMessage(), containsString("duplicate key error"));
        }
    }

    @Test
    void givenADescriptorIsSaved_whenItIsRetrieved_thenItsCreatedModifiedAndVersionShouldBeFilled() {
        Descriptor descriptorToSave = new DescriptorSavers(descriptorService)
                .createSingleDescriptor(new ObjectId().toString(), StandardStorageType.MONGO, "iri");
        Descriptor savedDescriptor = reactiveDescriptorDao.store(descriptorToSave).block();
        assertThat(savedDescriptor, is(notNullValue()));

        Descriptor retrievedDescriptor = reactiveDescriptorDao.retrieveByExternalId(savedDescriptor.getExternalId())
                .block();
        assertThat(retrievedDescriptor, is(notNullValue()));

        assertThatAutoFieldsAreFilledCorrectly(retrievedDescriptor);
    }

    private void assertThatAutoFieldsAreFilledCorrectly(Descriptor retrievedDescriptor) {
        assertThat(retrievedDescriptor.getCreated(), is(notNullValue()));
        assertThat(retrievedDescriptor.getModified(), is(notNullValue()));
        assertThat(retrievedDescriptor.getVersion(), is(0L));
    }

    @Test
    void givenADescriptorExists_whenRetrievingItWithInternalIdsCollection_thenItsExternalIdShouldBeReturnedInAMap() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        Mono<Map<String, String>> mono = reactiveDescriptorDao.retrieveMapByInternalIds(
                singletonList(objectId.toString()));

        StepVerifier.create(mono)
                .expectNext(singletonMap(descriptor.getInternalId(), descriptor.getExternalId()))
                .verifyComplete();
    }

    @Test
    void givenDescriptorExistsOnlyInMongoButNotInRedis_whenRetrievingItThenItShouldBeFound() {
        String descriptorExternalId = createDescriptorAndReturnExternalId();
        Descriptor removedFromRedis = descriptorsInRedis.remove(descriptorExternalId);
        assertThat(removedFromRedis, is(notNullValue()));
        assertThat(descriptorsInRedis.get(descriptorExternalId), is(nullValue()));

        ReactiveDescriptorDao descriptorDaoWithEmptyCaches = ReactiveDescriptorDaoFactory.create(
                redisProperties, descriptorsProperties, redissonReactiveClient, descriptorRepository,
                descriptorsMongoOperations, reactiveMongoDatabaseFactory);
        Descriptor retrievedFromMongo = descriptorDaoWithEmptyCaches.retrieveByExternalId(descriptorExternalId)
                .block();

        assertThat(retrievedFromMongo, is(notNullValue()));
        assertThat(retrievedFromMongo.getExternalId(), equalTo(descriptorExternalId));
    }

    @Test
    void givenDescriptorExistsOnlyInMongoButNotInRedis_whenRetrievingItThenItShouldBeFoundAndReinsertedToRedis() {
        String descriptorExternalId = createDescriptorAndReturnExternalId();
        Descriptor removedFromRedis = descriptorsInRedis.remove(descriptorExternalId);
        assertThat(removedFromRedis, is(notNullValue()));
        assertThat(descriptorsInRedis.get(descriptorExternalId), is(nullValue()));

        ReactiveDescriptorDao descriptorDaoWithEmptyCaches = ReactiveDescriptorDaoFactory.create(
                redisProperties, descriptorsProperties, redissonReactiveClient, descriptorRepository,
                descriptorsMongoOperations, reactiveMongoDatabaseFactory);
        descriptorDaoWithEmptyCaches.retrieveByExternalId(descriptorExternalId).block();

        Descriptor descriptorReaddedToRedis = descriptorsInRedis.get(descriptorExternalId);
        assertThat(descriptorReaddedToRedis, is(notNullValue()));
        assertThat(descriptorReaddedToRedis.getExternalId(), equalTo(descriptorExternalId));
    }

    private String createDescriptorAndReturnExternalId() {
        Descriptor descriptor = saveADescriptor();
        return descriptor.getExternalId();
    }

    @Test
    void destroyedDescriptorShouldBeRemovedFromMongo() {
        Descriptor descriptor = saveADescriptor();

        reactiveDescriptorDao.destroy(descriptor.getExternalId()).block();

        Query query = new Query(where("_id").is(descriptor.getExternalId()));
        Descriptor fromMongo = descriptorsMongoOperations.findOne(query, Descriptor.class).block();
        assertThat(fromMongo, is(nullValue()));
    }

    private Descriptor saveADescriptor() {
        return descriptorSaver.createAndSave(new ObjectId().toString(), "TestModel", StandardStorageType.MONGO, "iri", Collections.emptyMap());
    }

    @Test
    void destroyedDescriptorShouldBeRemovedFromRedisDescriptorsMap() {
        Descriptor descriptor = saveADescriptor();

        reactiveDescriptorDao.destroy(descriptor.getExternalId()).block();

        Descriptor fromRedis = descriptorsInRedis.get(descriptor.getExternalId());
        assertThat(fromRedis, is(nullValue()));
    }

    @Test
    void destroyedDescriptorShouldBeRemovedFromRedisInternalIdsMap() {
        Descriptor descriptor = saveADescriptor();

        reactiveDescriptorDao.destroy(descriptor.getExternalId()).block();

        RMap<String, String> internalIdsMap = redissonClient.getMap(descriptorsProperties.getInternalIdsMapName(),
                new StringCodec());

        assertThat(internalIdsMap.get(descriptor.getInternalId()), is(nullValue()));
    }

    @Test
    void destroyedCollectionDescriptorShouldBeRemovedFromRedisCoordinateStringsMap() {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree(randomString());
        Descriptor descriptor = descriptorSaver.createAndSave(collectionDescriptor);

        reactiveDescriptorDao.destroy(descriptor.getExternalId()).block();

        RMap<String, String> collectionCoordinatesMap = redissonClient.getMap(
                descriptorsProperties.getCollectionCoordinatesMapName(), new StringCodec());

        assertThat(collectionCoordinatesMap.get(collectionDescriptor.getCoordinatesString()), is(nullValue()));
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    void shouldAllowDestroyingNonExistentDescriptor() {
        reactiveDescriptorDao.destroy("no-such-descriptor-id").block();
    }

    @Test
    void shouldRetrieveByIri() {
        ObjectId objectId = new ObjectId();
        Descriptor originalDescriptor = mongoDescriptorFacilities.create(objectId, "test_model", "\\/a\\/b\\/c", Collections.emptyMap());

        String externalId = originalDescriptor.getExternalId();
        assertNotNull(externalId);

        String iri = originalDescriptor.getIri();
        assertNotNull(iri);

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByIri("\\/a\\/b\\/c");
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }
}
