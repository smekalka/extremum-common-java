package io.extremum.jpa.dao;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.utils.ModelUtils;
import io.extremum.jpa.TestWithServices;
import io.extremum.jpa.model.TestJpaModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
class JpaCommonDaoTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestJpaModelDao dao;

    @Autowired
    private DescriptorService descriptorService;

    @Test
    void whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestJpaModel model = new TestJpaModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestJpaModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getUuid());
        assertNotNull(model.getCreated());
        assertNotNull(model.getModified());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    void whenFinding_thenDescriptorShouldBeFilled() {
        TestJpaModel savedModel = dao.save(new TestJpaModel());

        TestJpaModel loadedModel = dao.findById(savedModel.getId())
                .orElseThrow(() -> new AssertionError("Did not find anything"));

        assertThat(loadedModel.getUuid(), is(notNullValue()));
    }

    @Test
    void whenFinding_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestJpaModel savedModel = dao.save(new TestJpaModel());

        TestJpaModel loadedModel = dao.findById(savedModel.getId())
                .orElseThrow(() -> new AssertionError("Did not find anything"));

        assertThat(loadedModel.getUuid().getInternalId(), is(equalTo(savedModel.getId().toString())));
    }

    @Test
    void testCreateModelWithWrongVersion() {
        TestJpaModel model = getTestModel();
        model = dao.save(model);
        model.setName(UUID.randomUUID().toString());
        model = dao.save(model);

        assertThat(model.getVersion(), is(1L));

        model.setVersion(0L);
        try {
            dao.save(model);
            fail("An optimistic locking failure should have occurred");
        } catch (OptimisticLockingFailureException e) {
            // expected
        }
    }

    @Test
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestJpaModel> modelList = Stream
                .generate(this::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestJpaModel> createdModelList = dao.saveAll(modelList);
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
        TestJpaModel model = getTestModel();
        dao.save(model);

        TestJpaModel resultModel = dao.findById(model.getId())
                .orElseThrow(this::didNotFindAnything);
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());

        resultModel = dao.findById(UUID.randomUUID()).orElse(null);
        assertNull(resultModel);

        TestJpaModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModel = dao.findById(deletedModel.getId()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void testListAll() {
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
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestJpaModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(1));
    }

    @Test
    @Disabled("Restore when we have a decent mechanism to ignore softly-deleted records on Spring Data level")
    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestJpaModel> results = dao.findEvenDeletedByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countByName(uniqueName), is(1L));
    }

    @Test
    @Disabled("Restore when we have a decent mechanism to ignore softly-deleted records on Spring Data level")
    void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countEvenDeletedByName(uniqueName), is(2L));
    }

    @NotNull
    private List<TestJpaModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestJpaModel notDeleted = new TestJpaModel();
        notDeleted.setName(uniqueName);

        TestJpaModel deleted = new TestJpaModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }
    
    @Test
    void givenEntityExists_whenCallingDeleteById_thenItShouldNotBeFoundLater() {
        TestJpaModel model = dao.save(new TestJpaModel());

        dao.deleteById(model.getId());

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        TestJpaModel model = dao.save(new TestJpaModel());

        TestJpaModel deletedModel = dao.deleteByIdAndReturn(model.getId());
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void testAllInBatchDeletionIsDisabled() {
        try {
            dao.deleteAllInBatch();
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("We don't allow to delete all the records in one go"));
        }
    }

    @Test
    void testGetModelNameAnnotation_OnHibernateProxy_AndOriginalClass() {
        TestJpaModel testModel = getTestModel();
        testModel.setName("test");
        dao.save(testModel);
        TestJpaModel proxy = dao.getOne(testModel.getId());
        TestJpaModel model = dao.findById(testModel.getId())
                .orElseThrow(this::didNotFindAnything);
        assertThat(ModelUtils.getModelName(model), is("TestJpaModel"));
        assertThat(ModelUtils.getModelName(proxy), is("TestJpaModel"));
    }

    @NotNull
    private AssertionError didNotFindAnything() {
        return new AssertionError("Did not find");
    }

    @Test
    void testDeletionOfAListInBatch() {
        TestJpaModel model1 = dao.save(new TestJpaModel());
        TestJpaModel model2 = dao.save(new TestJpaModel());

        dao.deleteInBatch(Arrays.asList(model1, model2));
    }

    @Test
    void givenAnEntityIsNew_whenItIsSaved_thenEntityIdShouldMatchDescriptorInternalId() {
        TestJpaModel model = dao.save(new TestJpaModel());

        assertThat(model.getId().toString(), is(model.getUuid().getInternalId()));
    }

    private TestJpaModel getDeletedTestModel() {
        TestJpaModel model = getTestModel();
        model.setDeleted(true);
        return model;
    }

    private TestJpaModel getTestModel() {
        TestJpaModel model = new TestJpaModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(UUID.randomUUID().toString())
                .modelType(ModelUtils.getModelName(model))
                .storageType(StandardStorageType.POSTGRES)
                .build();

        descriptorService.store(descriptor);
        model.setUuid(descriptor);
        return model;
    }
}
