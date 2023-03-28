package common.dao.mongo;

import io.extremum.common.test.TestWithServices;
import models.TestMongoModel;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class ReactiveMongoCommonDaoModelLifecycleTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestReactiveMongoModelDao dao;

    @Test
    void freshModelShouldNotHaveSystemFieldsFilled() {
        TestMongoModel model = new TestMongoModel();

        assertNull(model.getUuid());
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());
    }

    @Test
    void whenInserting_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel savedModel = dao.insert(new TestMongoModel()).block();

        assertThatSystemFieldsAreFilled(savedModel);
    }

    private void assertThatSystemFieldsAreFilled(TestMongoModel createdModel) {
        assertNotNull(createdModel.getId());
        assertNotNull(createdModel.getUuid());
        assertNotNull(createdModel.getCreated());
        assertNotNull(createdModel.getVersion());
        assertFalse(createdModel.getDeleted());
    }

    @Test
    void whenInsertingAll_thenAllAutoFieldsShouldBeFilled() {
        List<TestMongoModel> savedModels = dao.insert(singletonList(new TestMongoModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void whenInsertingAllWithPublisher_thenAllAutoFieldsShouldBeFilled() {
        List<TestMongoModel> savedModels = dao.insert(Flux.just(new TestMongoModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void givenEntityIsNew_whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel savedModel = dao.save(new TestMongoModel()).block();

        assertThatSystemFieldsAreFilled(savedModel);
    }

    @Test
    void givenEntityIsNew_whenSavingAll_thenAllAutoFieldsShouldBeFilled() {
        List<TestMongoModel> savedModels = dao.saveAll(singletonList(new TestMongoModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void givenEntityIsNew_whenSavingAllWithPublisher_thenAllAutoFieldsShouldBeFilled() {
        List<TestMongoModel> savedModels = dao.saveAll(Flux.just(new TestMongoModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void givenEntityIsNotNew_whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel savedModel = dao.save(new TestMongoModel()).block();
        savedModel.setName(randomName());
        TestMongoModel resavedModel = dao.save(savedModel).block();

        assertThatSystemFieldsAreFilled(resavedModel);
    }

    @Test
    void givenEntityIsNotNew_whenSavingAll_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel savedModel = dao.save(new TestMongoModel()).block();
        savedModel.setName(randomName());
        List<TestMongoModel> resavedModels = dao.saveAll(singletonList(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(resavedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(resavedModels.get(0));
    }

    @Test
    void givenEntityIsNotNew_whenSavingAllWithPublisher_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel savedModel = dao.save(new TestMongoModel()).block();
        savedModel.setName(randomName());
        List<TestMongoModel> resavedModels = dao.saveAll(Flux.just(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(resavedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(resavedModels.get(0));
    }

    @Test
    void whenFindingById_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    private TestMongoModel saveATestModel() {
        TestMongoModel modelToSave = new TestMongoModel();
        modelToSave.setName(randomName());
        return dao.save(modelToSave).block();
    }

    @NotNull
    private String randomName() {
        return UUID.randomUUID().toString();
    }

    private static Matcher<TestMongoModel> hasNotNullUuid() {
        return Matchers.hasProperty("uuid", is(notNullValue()));
    }

    @Test
    void whenFindingById_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    private Matcher<TestMongoModel> hasUuidConsistentWithId() {
        return new ConsistentUuidIdMatcher();
    }

    @Test
    void whenFindingByIdPublisher_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findById(Mono.just(savedModel.getId())).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenFindingByIdPublisher_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findById(Mono.just(savedModel.getId())).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    @Test
    void whenFindingAllById_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAllById(singletonList(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllById_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAllById(singletonList(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingAllByIdPublisher_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAllById(Flux.just(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllByIdPublisher_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAllById(Flux.just(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingOneByExample_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findOne(Example.of(savedModel)).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenFindingOneByExample_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findOne(Example.of(savedModel)).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    @Test
    void whenFindingAll_thenDescriptorShouldBeFilled() {
        saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll()
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAll_thenDescriptorInternalIdShouldMatchTheEntityId() {
        saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll()
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingAllWithSort_thenDescriptorShouldBeFilled() {
        saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll(Sort.by("name"))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllWithSort_thenDescriptorInternalIdShouldMatchTheEntityId() {
        saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll(Sort.by("name"))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingAllByExample_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll(Example.of(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllByExample_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll(Example.of(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingAllByExampleWithSort_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll(Example.of(savedModel), Sort.by("name"))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllByExampleWithSort_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAll(Example.of(savedModel), Sort.by("name"))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingMonoWithGeneratedQueryMethod_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findOneByName(savedModel.getName()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenFindingMonoWithGeneratedQueryMethod_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.findOneByName(savedModel.getName()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    @Test
    void whenFindingFluxWithGeneratedQueryMethod_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAllByName(savedModel.getName())
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingFluxWithGeneratedQueryMethod_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        List<TestMongoModel> loadedModels = dao.findAllByName(savedModel.getName())
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenDeletingByIdAndReturning_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenDeletingByIdAndReturning_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = saveATestModel();

        TestMongoModel loadedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    private static class ConsistentUuidIdMatcher extends TypeSafeDiagnosingMatcher<TestMongoModel> {
        @Override
        protected boolean matchesSafely(TestMongoModel item, Description mismatchDescription) {
            if (item.getUuid() == null) {
                mismatchDescription.appendText("uuid is null");
                return false;
            }
            if (item.getId() == null) {
                mismatchDescription.appendText("id is null");
                return false;
            }
            if (!Objects.equals(item.getUuid().getInternalId(), item.getId().toString())) {
                mismatchDescription.appendText("uuid.internalId '")
                        .appendValue(item.getUuid().getInternalId())
                        .appendText("' not equal to model id '")
                        .appendValue(item.getId().toString());
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("model with uuid.internalId equal to id.toString()");
        }
    }
}
