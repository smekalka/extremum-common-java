package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.descriptors.sync.dao.DescriptorDaoFactory;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.content.MediaType;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.Descriptor.Readiness;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorDaoTest extends TestWithServices {
    @Autowired
    private DescriptorDao descriptorDao;
    @Autowired
    private DescriptorRepository descriptorRepository;

    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;
    @Autowired
    private DescriptorSaver descriptorSaver;
    @Autowired
    @DescriptorsMongoDb
    private MongoOperations descriptorMongoOperations;

    private DescriptorDao freshDaoToAvoidCachingInMemory;

    @BeforeEach
    void init() {
        freshDaoToAvoidCachingInMemory = DescriptorDaoFactory.createBaseDescriptorDao(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository, descriptorMongoOperations);
    }

    @Test
    void testRetrieveByExternalId() {
        Descriptor descriptor = saveADescriptor();

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    private Descriptor saveADescriptor() {
        ObjectId objectId = new ObjectId();
        return mongoDescriptorFacilities.create(objectId, "test_model", "iri", new HashMap<>());
    }

    @Test
    void testRetrieveByInternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", new HashMap<>());

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(objectId.toString());
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    void testRetrieveByCollectionCoordinates() {
        ObjectId hostId = new ObjectId();
        Descriptor hostDescriptor = mongoDescriptorFacilities.create(hostId, "test_model", "iri", Collections.emptyMap());

        Descriptor descriptor = Descriptor.forCollection("external-id",
                CollectionDescriptor.forOwned(hostDescriptor, "attr"));
        descriptorDao.store(descriptor);

        Descriptor retrievedDescriptor = descriptorDao.retrieveByCollectionCoordinates(
                descriptor.getCollection().toCoordinatesString()).orElse(null);
        assertThat(retrievedDescriptor, is(notNullValue()));
        assertThatRetrievedCollectionIsAsExpected(descriptor, retrievedDescriptor);
    }

    private void assertThatRetrievedCollectionIsAsExpected(Descriptor descriptor, Descriptor retrievedDescriptor) {
        assertEquals(descriptor.getExternalId(), retrievedDescriptor.getExternalId());
        assertThat(retrievedDescriptor.getType(), is(Descriptor.Type.COLLECTION));
        assertThat(retrievedDescriptor.getCollection(), is(notNullValue()));
        assertThat(retrievedDescriptor.getCollection().getType(), is(CollectionDescriptor.Type.OWNED));
        assertThat(retrievedDescriptor.getCollection().getCoordinatesString(),
                is(equalTo(descriptor.getCollection().toCoordinatesString())));
    }

    @Test
    void testRetrieveMapByExternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByExternalIds(Collections.singleton(externalId));
        assertEquals(1, retrievedMap.size());
        assertEquals(objectId.toString(), retrievedMap.get(externalId));
    }

    @Test
    void testRetrieveMapByInternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model", "iri", Collections.emptyMap());

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByInternalIds(Collections.singleton(objectId.toString()));
        assertEquals(1, retrievedMap.size());
        assertEquals(externalId, retrievedMap.get(objectId.toString()));
    }

    @Test
    void testRetrieveFromMongo() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = Descriptor.builder()
                .externalId(createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .build();

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(internalId);
        assertFalse(retrievedDescriptor.isPresent());

        descriptorMongoOperations.save(descriptor);
        retrievedDescriptor = descriptorDao.retrieveByInternalId(internalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor.getExternalId(), retrievedDescriptor.get().getExternalId());
        assertEquals(descriptor.getInternalId(), retrievedDescriptor.get().getInternalId());
    }

    @NotNull
    private String createExternalId() {
        return descriptorService.createExternalId();
    }

    @Test
    void testSaveDisplayFieldAsNull() {
        String internalId = new ObjectId().toString();
        String externalId = createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = freshDaoToAvoidCachingInMemory.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertNull(retrieved.get().getDisplay());
    }

    @Test
    void testSaveDisplayFieldAsString() {
        String internalId = new ObjectId().toString();
        String externalId = createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .display(new Display("abcd"))
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = freshDaoToAvoidCachingInMemory.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().getDisplay().isString());
        assertEquals("abcd", retrieved.get().getDisplay().getStringValue());
    }

    @Test
    void testDisplayFieldDeserialization() {
        Media iconObj = new Media();
        iconObj.setUrl("/url/to/resource");
        iconObj.setType(MediaType.IMAGE);
        iconObj.setWidth(100);
        iconObj.setHeight(200);
        iconObj.setDepth(2);
        iconObj.setDuration(new IntegerOrString(20));

        Display displayObj = new Display(
                new StringOrMultilingual("aaa"),
                iconObj,
                iconObj
        );

        String internalId = new ObjectId().toString();
        String externalId = createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .display(displayObj)
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = freshDaoToAvoidCachingInMemory.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());

        Descriptor retrievedDescriptor = retrieved.get();
        Display display = retrievedDescriptor.getDisplay();

        assertNotNull(display);
        assertTrue(display.isObject());
        Media icon = display.getIcon();
        assertNotNull(icon);

        assertEquals("/url/to/resource", icon.getUrl());
        assertEquals(MediaType.IMAGE, icon.getType());
        assertEquals(100, (int) icon.getWidth());
        assertEquals(200, (int) icon.getHeight());
        assertEquals(2, (int) icon.getDepth());
        assertNotNull(icon.getDuration());
        assertTrue(icon.getDuration().isInteger());
        assertEquals(20, (int) icon.getDuration().getIntegerValue());

        Media splash = display.getSplash();
        assertNotNull(splash);

        assertEquals("/url/to/resource", splash.getUrl());
        assertEquals(MediaType.IMAGE, splash.getType());
        assertEquals(100, (int) splash.getWidth());
        assertEquals(200, (int) splash.getHeight());
        assertEquals(2, (int) splash.getDepth());
        assertNotNull(splash.getDuration());
        assertTrue(splash.getDuration().isInteger());
        assertEquals(20, (int) splash.getDuration().getIntegerValue());
    }

    @Test
    void givenADescriptorExists_whenItIsSearchedFor_thenItShouldBeFound() {
        Descriptor descriptor = saveADescriptor();

        Optional<Descriptor> optDescriptor = descriptorRepository.findByExternalId(descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(true));
    }

    @Test
    void givenADescriptorIsSoftDeleted_whenItIsSearchedFor_thenItShouldNotBeFound() {
        Descriptor descriptor = saveADescriptor();

        descriptor.setDeleted(true);
        descriptorMongoOperations.save(descriptor);

        Optional<Descriptor> optDescriptor = descriptorRepository.findByExternalId(descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(false));
    }

    @Test
    void givenADescriptorWithAnInternalIdAlreadyExists_whenSavingAnotherDescriptorWithTheSameInternalId_thenAnExceptionShouldBeThrown() {
        Descriptor descriptor = saveADescriptor();

        try {
            mongoDescriptorFacilities.create(new ObjectId(descriptor.getInternalId()), "test_model", "iri", Collections.emptyMap());
            fail("An exception should be thrown");
        } catch (DuplicateKeyException e) {
            assertThat(e.getMessage(), containsString("duplicate key error"));
        }
    }

    @Test
    void givenACollectionDescriptorAlreadyExists_whenSavingAnotherCollectionDescriptorWithTheSameCoordinates_thenAnExceptionShouldBeThrown() {
        Descriptor host = saveADescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(host, "items");
        descriptorSaver.createAndSave(collectionDescriptor);

        try {
            descriptorSaver.createAndSave(collectionDescriptor);
            fail("An exception should be thrown");
        } catch (DuplicateKeyException e) {
            assertThat(e.getMessage(), containsString("duplicate key error"));
        }
    }

    @Test
    void whenStoringABatchOfDescriptors_thenTheyShouldBeSavedAndBecomeRetrievable() {
        String internalId1 = new ObjectId().toString();
        String internalId2 = new ObjectId().toString();
        DescriptorSavers savers = new DescriptorSavers(descriptorService);
        List<Descriptor> descriptorsToSave = Stream.of(internalId1, internalId2)
                .map(internalId -> savers.createSingleDescriptor(internalId, StandardStorageType.MONGO, "iri"))
                .collect(Collectors.toList());

        List<Descriptor> savedDescriptors = descriptorDao.storeBatch(descriptorsToSave);

        assertThat(savedDescriptors, hasSize(2));
        assertThat(savedDescriptors.get(0).getInternalId(), is(equalTo(internalId1)));
        assertThat(savedDescriptors.get(1).getInternalId(), is(equalTo(internalId2)));

        String externalId1 = savedDescriptors.get(0).getExternalId();
        Descriptor retrieved1 = descriptorDao.retrieveByExternalId(externalId1)
                .orElseThrow(() -> new AssertionError("Did not find anything"));
        assertThat(retrieved1.getInternalId(), is(equalTo(internalId1)));

        retrieved1 = descriptorDao.retrieveByInternalId(internalId1)
                .orElseThrow(() -> new AssertionError("Did not find anything"));
        assertThat(retrieved1.getExternalId(), is(equalTo(externalId1)));
    }

    @Test
    void givenABlankDescriptorWasRetrievedFromOneDao_whenItIsMadeReadyThrowAnotherDao_thenItShouldBeRetrievedFromFirstDaoAsReady() {
        // given
        DescriptorDao anotherDao = DescriptorDaoFactory.createBaseDescriptorDao(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository, descriptorMongoOperations);
        Descriptor descriptor = createBlankDescriptor();
        Descriptor storedDescriptor = descriptorDao.store(descriptor);
        anotherDao.retrieveByExternalId(storedDescriptor.getExternalId());

        // when
        descriptorService.makeDescriptorReady(descriptor.getExternalId(), "TestModel");

        // then
        Descriptor retrievedDescriptor = anotherDao.retrieveByExternalId(storedDescriptor.getExternalId())
                .orElseThrow(() -> new AssertionError("Did not find anything"));
        assertThat("Probably an old cached copy was retrieved",
                retrievedDescriptor.getReadiness(), is(Readiness.READY));
    }

    @NotNull
    private Descriptor createBlankDescriptor() {
        Descriptor descriptor = createMongoModelDescriptor();
        descriptor.setReadiness(Readiness.BLANK);
        return descriptor;
    }

    private Descriptor createMongoModelDescriptor() {
        DescriptorSavers savers = new DescriptorSavers(descriptorService);
        return savers.createSingleDescriptor(new ObjectId().toString(), StandardStorageType.MONGO, "iri");
    }

    @Test
    void whenDestroingDescriptorsInABatch_thenTheyShouldBeDestroyed() {
        Descriptor descriptor = descriptorDao.store(createBlankDescriptor());

        descriptorDao.destroyBatch(singletonList(descriptor));

        Optional<Descriptor> byExternalId = descriptorDao.retrieveByExternalId(descriptor.getExternalId());
        assertThat(byExternalId.isPresent(), is(false));

        Optional<Descriptor> byInternalId = descriptorDao.retrieveByInternalId(descriptor.getInternalId());
        assertThat(byInternalId.isPresent(), is(false));
    }

    @Test
    void givenADescriptorIsSaved_whenItIsRetrieved_thenItsCreatedModifiedAndVersionShouldBeFilled() {
        Descriptor descriptorToSave = new DescriptorSavers(descriptorService)
                .createSingleDescriptor(new ObjectId().toString(), StandardStorageType.MONGO, "iri");
        Descriptor savedDescriptor = descriptorDao.store(descriptorToSave);

        Descriptor retrievedDescriptor = freshDaoToAvoidCachingInMemory
                .retrieveByExternalId(savedDescriptor.getExternalId())
                .orElseThrow(() -> new AssertionError("Did not find the descriptor"));

        assertThatAutoFieldsAreFilledCorrectly(retrievedDescriptor);
    }

    private void assertThatAutoFieldsAreFilledCorrectly(Descriptor retrievedDescriptor) {
        assertThat(retrievedDescriptor.getCreated(), is(notNullValue()));
        assertThat(retrievedDescriptor.getModified(), is(notNullValue()));
        assertThat(retrievedDescriptor.getVersion(), is(0L));
    }

    @Test
    void givenADescriptorIsSavedInABatch_whenItIsRetrieved_thenItsCreatedModifiedAndVersionShouldBeFilled() {
        Descriptor descriptorToSave = new DescriptorSavers(descriptorService)
                .createSingleDescriptor(new ObjectId().toString(), StandardStorageType.MONGO, "iri");
        List<Descriptor> savedDescriptors = descriptorDao.storeBatch(singletonList(descriptorToSave));
        Descriptor savedDescriptor = savedDescriptors.get(0);

        Descriptor retrievedDescriptor = freshDaoToAvoidCachingInMemory
                .retrieveByExternalId(savedDescriptor.getExternalId())
                .orElseThrow(() -> new AssertionError("Did not find the descriptor"));

        assertThatAutoFieldsAreFilledCorrectly(retrievedDescriptor);
    }
}
