package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.TestWithServices;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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


@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class ReactiveElasticsearchCommonDaoModelLifecycleTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestReactiveElasticsearchModelDao dao;

    @Test
    void freshModelShouldNotHaveSystemFieldsFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();

        assertNull(model.getUuid());
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());
    }

    private void assertThatSystemFieldsAreFilled(TestElasticsearchModel createdModel) {
        assertNotNull(createdModel.getId());
        assertNotNull(createdModel.getUuid());
        assertNotNull(createdModel.getCreated());
        assertNotNull(createdModel.getVersion());
        assertFalse(createdModel.getDeleted());
    }

    @Test
    void givenEntityIsNew_whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();

        assertThatSystemFieldsAreFilled(savedModel);
    }

    @Test
    void givenEntityIsNew_whenSavingAll_thenAllAutoFieldsShouldBeFilled() {
        List<TestElasticsearchModel> savedModels = dao.saveAll(singletonList(new TestElasticsearchModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void givenEntityIsNew_whenSavingAllWithPublisher_thenAllAutoFieldsShouldBeFilled() {
        List<TestElasticsearchModel> savedModels = dao.saveAll(Flux.just(new TestElasticsearchModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void givenEntityIsNotNew_whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();
        savedModel.setName(randomName());
        TestElasticsearchModel resavedModel = dao.save(savedModel).block();

        assertThatSystemFieldsAreFilled(resavedModel);
    }

    @Test
    void givenEntityIsNotNew_whenSavingAll_thenAllAutoFieldsShouldBeFilled() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();
        savedModel.setName(randomName());
        List<TestElasticsearchModel> resavedModels = dao.saveAll(singletonList(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(resavedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(resavedModels.get(0));
    }

    @Test
    void givenEntityIsNotNew_whenSavingAllWithPublisher_thenAllAutoFieldsShouldBeFilled() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();
        savedModel.setName(randomName());
        List<TestElasticsearchModel> resavedModels = dao.saveAll(Flux.just(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(resavedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(resavedModels.get(0));
    }

    @Test
    void whenFindingById_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    private TestElasticsearchModel saveATestModel() {
        TestElasticsearchModel modelToSave = new TestElasticsearchModel();
        modelToSave.setName(randomName());
        return dao.save(modelToSave).block();
    }

    @NotNull
    private String randomName() {
        return UUID.randomUUID().toString();
    }

    private static Matcher<TestElasticsearchModel> hasNotNullUuid() {
        return Matchers.hasProperty("uuid", is(notNullValue()));
    }

    @Test
    void whenFindingById_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    private Matcher<TestElasticsearchModel> hasUuidConsistentWithId() {
        return new ConsistentUuidIdMatcher();
    }

    @Test
    void whenFindingByIdPublisher_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.findById(Mono.just(savedModel.getId())).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenFindingByIdPublisher_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.findById(Mono.just(savedModel.getId())).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    @Test
    void whenFindingAllById_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.findAllById(singletonList(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllById_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.findAllById(singletonList(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingAllByIdPublisher_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.findAllById(Flux.just(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAllByIdPublisher_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.findAllById(Flux.just(savedModel.getId()))
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenSearching_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.search(savedModel.getName(), SearchOptions.defaults())
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenSearching_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.search(savedModel.getName(), SearchOptions.defaults())
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenFindingMonoWithGeneratedQueryMethod_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.findOneByName(savedModel.getName()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenFindingMonoWithGeneratedQueryMethod_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.findOneByName(savedModel.getName()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    @Test
    void whenFindingFluxWithGeneratedQueryMethod_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.findAllByName(savedModel.getName())
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingFluxWithGeneratedQueryMethod_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        List<TestElasticsearchModel> loadedModels = dao.findAllByName(savedModel.getName())
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenDeletingByIdAndReturning_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenDeletingByIdAndReturning_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = saveATestModel();

        TestElasticsearchModel loadedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    private static class ConsistentUuidIdMatcher extends TypeSafeDiagnosingMatcher<TestElasticsearchModel> {
        @Override
        protected boolean matchesSafely(TestElasticsearchModel item, Description mismatchDescription) {
            if (item.getUuid() == null) {
                mismatchDescription.appendText("uuid is null");
                return false;
            }
            if (item.getId() == null) {
                mismatchDescription.appendText("id is null");
                return false;
            }
            if (!Objects.equals(item.getUuid().getInternalId(), item.getId())) {
                mismatchDescription.appendText("uuid.internalId '")
                        .appendValue(item.getUuid().getInternalId())
                        .appendText("' not equal to model id '")
                        .appendValue(item.getId());
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("model with uuid.internalId equal to uuid.id.toString()");
        }
    }
}
