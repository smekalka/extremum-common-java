package common.dao.mongo;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class ReactiveMongoCommonDaoTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestReactiveMongoModelDao dao;
    @Autowired
    private DescriptorService descriptorService;

    @Test
    void testCreateModelWithWrongVersion() {
        TestMongoModel model = getTestModel();
        model.setId(new ObjectId(model.getUuid().getInternalId()));
        model.setVersion(123L);
        assertThrows(OptimisticLockingFailureException.class, () -> dao.save(model).block());
    }

    @Test
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestMongoModel> modelList = Stream
                .generate(this::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestMongoModel> createdModelList = dao.saveAll(modelList).collectList().block();
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
        dao.save(model).block();

        TestMongoModel resultModel = dao.findById(model.getId()).block();
        assertThat(resultModel, is(notNullValue()));
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());

        resultModel = dao.findById(new ObjectId()).block();
        assertNull(resultModel);

        TestMongoModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel).block();

        resultModel = dao.findById(deletedModel.getId()).block();
        assertNull(resultModel);
    }

    @Test
    void testGetByPublisher() {
        TestMongoModel model = getTestModel();
        dao.save(model).block();

        TestMongoModel resultModel = dao.findById(Mono.just(model.getId())).block();
        assertThat(resultModel, is(notNullValue()));
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());

        resultModel = dao.findById(Mono.just(new ObjectId())).block();
        assertNull(resultModel);

        TestMongoModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel).block();

        resultModel = dao.findById(Mono.just(deletedModel.getId())).block();
        assertNull(resultModel);
    }

    @Test
    void testFindAll() {
        int initCount = dao.findAll().collectList().block().size();
        int modelsToCreate = 10;

        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getTestModel()).block();
        }
        int count = dao.findAll().collectList().block().size();
        assertEquals(initCount + modelsToCreate, count);

        initCount = count;
        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getDeletedTestModel()).block();
        }
        count = dao.findAll().collectList().block().size();
        assertEquals(initCount, count);

        assertThat(dao.findAll(Sort.by("id")).toStream().collect(Collectors.toList()), hasSize(count));
    }

    @Test
    void givenOneExemplarEntityExists_whenInvokingFindAllByExample_thenOneDocumentShouldBeReturned() {
        TestMongoModel model = dao.save(new TestMongoModel()).block();

        List<TestMongoModel> all = dao.findAll(Example.of(model)).collectList().block();

        assertThat(all, hasSize(1));
        assertThat(all.get(0).getId(), is(equalTo(model.getId())));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExample_thenNothingShouldBeReturned() {
        TestMongoModel model = new TestMongoModel();
        model.setDeleted(true);
        dao.save(model).block();

        List<TestMongoModel> all = dao.findAll(Example.of(model)).collectList().block();

        assertThat(all, hasSize(0));
    }

    @Test
    void givenOneExemplarEntityExists_whenInvokingFindAllByExampleWithSort_thenOneDocumentShouldBeReturned() {
        TestMongoModel model = dao.save(new TestMongoModel()).block();

        List<TestMongoModel> all = dao.findAll(Example.of(model), Sort.by("id")).collectList().block();

        assertThat(all, hasSize(1));
        assertThat(all.get(0).getId(), is(equalTo(model.getId())));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExampleWithSort_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        List<TestMongoModel> all = dao.findAll(Example.of(model), Sort.by("id")).collectList().block();

        assertThat(all, hasSize(0));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingFindOneByExample_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        TestMongoModel result = dao.findOne(Example.of(model)).block();

        assertThat(result, is(nullValue()));
    }

    @Test
    void givenADeletedExemplarEntityExists_whenInvokingExistsByExample_thenFalseShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        assertThat(dao.exists(Example.of(model)).block(), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        assertThat(dao.existsById(model.getId()).block(), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsByIdPublisher_thenFalseShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        assertThat(dao.existsById(Mono.just(model.getId())).block(), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllById_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        List<TestMongoModel> all = dao.findAllById(singletonList(model.getId())).collectList().block();

        assertThat(all, hasSize(0));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllByIdPublisher_thenNothingShouldBeReturned() {
        TestMongoModel model = dao.save(getDeletedTestModel()).block();

        List<TestMongoModel> all = dao.findAllById(Flux.just(model.getId())).collectList().block();

        assertThat(all, hasSize(0));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestMongoModel> results = dao.findAllByName(uniqueName).collectList().block();
        assertThat(results, hasSize(1));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestMongoModel> results = dao.findEvenDeletedByName(uniqueName).collectList().block();
        assertThat(results, hasSize(2));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        assertThat(dao.countByName(uniqueName).block(), is(1L));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        assertThat(dao.countEvenDeletedByName(uniqueName).block(), is(2L));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeleted_thenItShouldNotBeFoundAnymore() {
        TestMongoModel model = new TestMongoModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteById(model.getId()).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeletedWithPublisher_thenItShouldNotBeFoundAnymore() {
        TestMongoModel model = new TestMongoModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteById(Mono.just(model.getId())).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeleted_thenItShouldNotBeFoundAnymoreWithPublisher() {
        TestMongoModel model = new TestMongoModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(Mono.just(model.getId())).block(), is(notNullValue()));

        dao.deleteById(model.getId()).block();

        assertThat(dao.findById(Mono.just(model.getId())).block(), is(nullValue()));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        TestMongoModel model = dao.save(new TestMongoModel()).block();

        TestMongoModel deletedModel = dao.deleteByIdAndReturn(model.getId()).block();
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenAnEntityIsNew_whenItIsSaved_thenEntityIdShouldMatchDescriptorInternalId() {
        TestMongoModel model = dao.save(new TestMongoModel()).block();

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
