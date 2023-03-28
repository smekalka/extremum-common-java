package common.dao.mongo;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.org.apache.commons.lang.math.RandomUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.extremum.common.model.PersistableCommonModel.FIELDS.created;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class MongoCommonDaoTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestMongoModelDao dao;
    @Autowired
    private DescriptorService descriptorService;

    @Test
    void whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel model = new TestMongoModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestMongoModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getUuid());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    void whenFinding_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = dao.save(new TestMongoModel());

        TestMongoModel loadedModel = dao.findById(savedModel.getId())
                .orElseThrow(() -> new AssertionError("Did not find anything"));

        assertThat(loadedModel.getUuid(), is(notNullValue()));
    }

    @Test
    void whenFinding_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = dao.save(new TestMongoModel());

        TestMongoModel loadedModel = dao.findById(savedModel.getId())
                .orElseThrow(() -> new AssertionError("Did not find anything"));

        assertThat(loadedModel.getUuid().getInternalId(), is(equalTo(savedModel.getId().toString())));
    }

    @Test
    void testCreateModelWithWrongVersion() {
        TestMongoModel model = getTestModel();
        model.setId(new ObjectId(model.getUuid().getInternalId()));
        model.setVersion(123L);
        assertThrows(OptimisticLockingFailureException.class, () -> dao.save(model));
    }

    @Test
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestMongoModel> modelList = Stream
                .generate(this::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestMongoModel> createdModelList = dao.saveAll(modelList);
        assertNotNull(createdModelList);
        assertEquals(modelsToCreate, createdModelList.size());

        long validCreated = createdModelList.stream()
                .filter(model -> modelList.contains(model) && model.getCreated() != null
                        && model.getVersion() != null && model.getId() != null)
                .count();
        assertEquals(modelsToCreate, validCreated);
    }

    @Test
    void testGet() {
        TestMongoModel model = getTestModel();
        dao.save(model);

        TestMongoModel resultModel = dao.findById(model.getId())
                .orElseThrow(() -> new AssertionError("Did not find"));
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());

        resultModel = dao.findById(new ObjectId()).orElse(null);
        assertNull(resultModel);

        TestMongoModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModel = dao.findById(deletedModel.getId()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void testGetByFieldValue() {
        TestMongoModel model = getTestModel();
        dao.save(model);

        List<TestMongoModel> resultModels = dao.listByFieldValue(created.name(), model.getCreated());
        assertEquals(1, resultModels.size());
        assertEquals(model.getId(), resultModels.get(0).getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModels.get(0).getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModels.get(0).getModified().toEpochSecond());
        assertEquals(model.getDeleted(), resultModels.get(0).getDeleted());
        assertEquals(model.getVersion(), resultModels.get(0).getVersion());

        resultModels = dao.listByFieldValue(created.name(), ZonedDateTime.now());
        assertTrue(resultModels.isEmpty());

        TestMongoModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModels = dao.listByFieldValue(created.name(), deletedModel.getCreated());
        assertTrue(resultModels.isEmpty());
    }

    @Test
    void testGetSelectedFieldsById() {
        TestMongoModel model = getTestModel();
        dao.save(model);

        String[] fields = {created.name()};
        TestMongoModel resultModel = dao.getSelectedFieldsById(model.getId(), fields).orElse(null);
        assertNotNull(resultModel);
        assertNotNull(resultModel.getId());
        assertNotNull(resultModel.getCreated());
        assertNull(resultModel.getModified());
        assertNull(resultModel.getVersion());

        TestMongoModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModel = dao.getSelectedFieldsById(deletedModel.getId(), fields).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void testFindAll() {
        int initCount = dao.findAll().size();
        int modelsToCreate = 10;

        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getTestModel());
        }
        int count = dao.findAll().size();
        assertEquals(initCount + modelsToCreate, count);

        initCount = count;
        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getDeletedTestModel());
        }
        count = dao.findAll().size();
        assertEquals(initCount, count);

        assertThat(dao.findAll(Sort.by("id")), hasSize(count));

        Page<TestMongoModel> page = dao.findAll(Pageable.unpaged());
        assertThat(page.getTotalElements(), is((long) count));
        assertThat(page.getContent(), hasSize(count));
    }

    @Test
    void givenOneExemplarEntityExists_whenInvokingFindAllByExample_thenOneDocumentShouldBeReturned() {
        TestMongoModel model = dao.save(new TestMongoModel());

        List<TestMongoModel> all = dao.findAll(Example.of(model));

        assertThat(all, hasSize(1));
        assertThat(all.get(0).getId(), is(equalTo(model.getId())));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExample_thenNothingShouldBeReturned() {
        TestMongoModel model = new TestMongoModel();
        model.setDeleted(true);
        dao.save(model);

        List<TestMongoModel> all = dao.findAll(Example.of(model));

        assertThat(all, hasSize(0));
    }

    @Test
    void givenOneExemplarEntityExists_whenInvokingFindAllByExampleWithSort_thenOneDocumentShouldBeReturned() {
        TestMongoModel model = dao.save(new TestMongoModel());

        List<TestMongoModel> all = dao.findAll(Example.of(model), Sort.by("id"));

        assertThat(all, hasSize(1));
        assertThat(all.get(0).getId(), is(equalTo(model.getId())));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExampleWithSort_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel());

        List<TestMongoModel> all = dao.findAll(Example.of(model), Sort.by("id"));

        assertThat(all, hasSize(0));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExampleWithPageable_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel());

        Page<TestMongoModel> page = dao.findAll(Example.of(model), Pageable.unpaged());

        assertThat(page.getTotalElements(), is(0L));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindOneByExample_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel());

        Optional<TestMongoModel> result = dao.findOne(Example.of(model));

        assertThat(result.isPresent(), is(false));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingExistsByExample_thenFalseShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel());

        assertThat(dao.exists(Example.of(model)), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel());

        assertThat(dao.existsById(model.getId()), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllById_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel());

        Iterable<TestMongoModel> all = dao.findAllById(Collections.singletonList(model.getId()));

        assertThat(all.iterator().hasNext(), is(false));
    }

    @Test
    void testListByParameters() {
        int initCount = dao.listByParameters(null).size();
        int modelsToCreate = 15;
        // limit = 0 означает выбор всего. Такая проверка выполняется отдельно
        int limit = RandomUtils.nextInt(modelsToCreate - 1) + 1;
        int offset = RandomUtils.nextInt(modelsToCreate);
        int idsSize = RandomUtils.nextInt(modelsToCreate);

        String name = UUID.randomUUID().toString();
        List<ObjectId> createdIds = new ArrayList<>();

        for (int i = 0; i < modelsToCreate; i++) {
            TestMongoModel testModel = getTestModel();
            testModel.setName(name);
            dao.save(testModel);
            createdIds.add(testModel.getId());
        }
        int count = dao.listByParameters(null).size();
        assertEquals(initCount + modelsToCreate, count);

        count = dao.listByParameters(Collections.emptyMap()).size();
        assertEquals(initCount + modelsToCreate, count);

        initCount = count;
        count = dao.listByParameters(Collections.singletonMap("limit", limit)).size();
        assertEquals(limit, count);

        count = dao.listByParameters(Collections.singletonMap("limit", 0)).size();
        assertEquals(initCount, count);

        count = dao.listByParameters(Collections.singletonMap("offset", offset)).size();
        assertEquals(initCount - offset, count);

        count = dao.listByParameters(Collections.singletonMap("ids", createdIds.subList(0, idsSize))).size();
        assertEquals(idsSize, count);

        count = dao.listByParameters(Collections.singletonMap(TestMongoModel.FIELDS.name.name(), name)).size();
        assertEquals(modelsToCreate, count);
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestMongoModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(1));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestMongoModel> results = dao.findEvenDeletedByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countByName(uniqueName), is(1L));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countEvenDeletedByName(uniqueName), is(2L));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeleted_thenItShouldNotBeFoundAnymore() {
        TestMongoModel model = new TestMongoModel();
        model.setName("Test");
        model = dao.save(model);

        assertThat(dao.findById(model.getId()).isPresent(), is(true));

        dao.deleteById(model.getId());

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        TestMongoModel model = dao.save(new TestMongoModel());

        TestMongoModel deletedModel = dao.deleteByIdAndReturn(model.getId());
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void givenAnEntityIsNew_whenItIsSaved_thenEntityIdShouldMatchDescriptorInternalId() {
        TestMongoModel model = dao.save(new TestMongoModel());

        assertThat(model.getId().toString(), is(model.getUuid().getInternalId()));
    }

    @NotNull
    private List<TestMongoModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestMongoModel notDeleted = new TestMongoModel();
        notDeleted.setName(uniqueName);

        TestMongoModel deleted = new TestMongoModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    private static TestMongoModel getDeletedTestModel() {
        TestMongoModel model = new TestMongoModel();
        model.setDeleted(true);
        return model;
    }

    private TestMongoModel getTestModel() {
        TestMongoModel model = new TestMongoModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .modelType(ModelUtils.getModelName(model.getClass()))
                .storageType(StandardStorageType.MONGO)
                .build();
        descriptorService.store(descriptor);

        model.setUuid(descriptor);
        return model;
    }
}
