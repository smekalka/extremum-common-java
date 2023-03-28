package io.extremum.elasticsearch.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.extremum.elasticsearch.TestWithServices;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class RepositoryBasedReactiveElasticsearchDaoTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestReactiveElasticsearchModelDao dao;
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private TestElasticsearchClient client;

    @BeforeEach
    void createClient() {
        client = new TestElasticsearchClient(elasticsearchProperties);
    }

    @Test
    void testCreateModel() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        assertNull(model.getId());
        assertNull(model.getUuid());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestElasticsearchModel createdModel = dao.save(model).block();
        assertSame(model, createdModel);
        assertThatSystemFieldsAreFilledAfterSave(createdModel);
    }

    private void assertThatSystemFieldsAreFilledAfterSave(TestElasticsearchModel createdModel) {
        assertNotNull(createdModel.getId(), "id");
        assertNotNull(createdModel.getUuid(), "uuid");
        assertNotNull(createdModel.getCreated(), "created");
        assertNotNull(createdModel.getVersion(), "version");
        assertFalse(createdModel.getDeleted(), "deleted");
    }

    @Test
    void whenAnEntityIsSavedTwice_thenTheVersionShouldBecome2() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model = dao.save(model).block();
        model.setName(UUID.randomUUID().toString());
        model = dao.save(model).block();

        assertThat(model.getVersion(), is(2L));
    }

    @Test
    void testCreateModelWithVersionConflict() {
        TestElasticsearchModel savedModel = makeSureModelHasSeqNumberMoreThanZero(new TestElasticsearchModel());

        savedModel.setSeqNoPrimaryTerm(new SeqNoPrimaryTerm(0, 1));
        savedModel.setName(UUID.randomUUID().toString());
        assertThrows(OptimisticLockingFailureException.class, () -> dao.save(savedModel).block());
    }

    @NotNull
    private TestElasticsearchModel makeSureModelHasSeqNumberMoreThanZero(TestElasticsearchModel model) {
        model = dao.save(model).block();
        model = dao.save(model).block();
        return model;
    }

    @Test
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestElasticsearchModel> modelList = Stream
                .generate(TestElasticsearchModel::new)
                .limit(modelsToCreate)
                .collect(Collectors.toList());
        modelList.forEach(model -> model.setName(UUID.randomUUID().toString()));

        List<TestElasticsearchModel> createdModelList = dao.saveAll(modelList)
                .toStream().collect(Collectors.toList());
        assertNotNull(createdModelList);
        assertEquals(modelsToCreate, createdModelList.size());

        long validCreated = createdModelList.stream()
                .filter(model -> modelList.contains(model) && model.getCreated() != null
                        && model.getVersion() != null && model.getId() != null)
                .count();
        assertEquals(modelsToCreate, validCreated);
    }

    @Test
    void givenAnEntityIsSaved_whenSavingItAgain_thenTheIdNorDescriptorShouldChange() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());
        dao.save(model).block();

        String externalId = model.getUuid().getExternalId();
        String internalId = model.getId();

        model.setName(UUID.randomUUID().toString());
        dao.save(model).block();

        assertThat(model.getId(), is(equalTo(internalId)));
        assertThat(model.getUuid().getExternalId(), is(equalTo(externalId)));
    }

    @Test
    void givenEntityExists_whenFindById_thenWeShouldFindTheEntity() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();

        TestElasticsearchModel resultModel = dao.findById(savedModel.getId()).block();
        assertEquals(savedModel.getId(), resultModel.getId());
        assertEquals(savedModel.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(savedModel.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(savedModel.getVersion(), resultModel.getVersion());
        assertEquals(savedModel.getDeleted(), resultModel.getDeleted());
    }

    @Test
    void givenEntityDoesNotExist_whenFindById_thenNothingShouldBeFound() {
        TestElasticsearchModel resultModel = dao.findById(UUID.randomUUID().toString()).block();
        assertNull(resultModel);
    }

    @Test
    void givenEntityIsDeleted_whenFindById_thenNothingShouldBeFound() {
        TestElasticsearchModel modelToBeDeleted = saveAndDeleteModel();

        TestElasticsearchModel resultModel = dao.findById(modelToBeDeleted.getId()).block();
        assertNull(resultModel);
    }

    @Test
    void whenSavingAnEntity_thenVersionSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        assertThat(model.getVersion(), is(notNullValue()));
        assertThat(model.getSeqNoPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindById_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        TestElasticsearchModel resultModel = dao.findById(model.getId()).block();

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNoPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindItWithSearch_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        List<TestElasticsearchModel> searchResult = dao.search(model.getId(), exactMatchSearch())
                .toStream().collect(Collectors.toList());
        assertThat(searchResult, hasSize(1));
        TestElasticsearchModel resultModel = searchResult.get(0);

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNoPrimaryTerm(), is(notNullValue()));
    }

    private SearchOptions exactMatchSearch() {
        return SearchOptions.builder().exactFieldValueMatch(true).build();
    }

    @Test
    void givenEntityIsCreated_whenFindItWithFindAll_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        List<TestElasticsearchModel> list = dao.findAllById(singletonList(model.getId()))
                .toStream().collect(Collectors.toList());
        assertThat(list, hasSize(1));
        TestElasticsearchModel resultModel = list.get(0);

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNoPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void testFindAll_throwsAnException() {
        assertThrows(UnsupportedOperationException.class, dao::findAll);
    }

    @Test
    void testFindAllWithSort_throwsAnException() {
        assertThrows(UnsupportedOperationException.class, () -> dao.findAll(Sort.by("id")));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestElasticsearchModel model = saveAndDeleteModel();

        assertThat(dao.existsById(model.getId()).block(), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsByIdWithPublisher_thenFalseShouldBeReturned() {
        TestElasticsearchModel model = saveAndDeleteModel();

        assertThat(dao.existsById(Mono.just(model.getId())).block(), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllById_thenNothingShouldBeReturned() {
        TestElasticsearchModel model = saveAndDeleteModel();

        Iterable<TestElasticsearchModel> all = dao.findAllById(singletonList(model.getId()))
                .toIterable();

        assertThat(all.iterator().hasNext(), is(false));
    }

    @NotNull
    private TestElasticsearchModel saveAndDeleteModel() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();
        dao.deleteById(model.getId()).block();
        return model;
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllByIdPublisher_thenNothingShouldBeReturned() {
        TestElasticsearchModel model = saveAndDeleteModel();

        Iterable<TestElasticsearchModel> all = dao.findAllById(Flux.just(model.getId())).toIterable();

        assertThat(all.iterator().hasNext(), is(false));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        TestElasticsearchModel model = dao.save(new TestElasticsearchModel()).block();

        TestElasticsearchModel deletedModel = dao.deleteByIdAndReturn(model.getId()).block();
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestElasticsearchModel> results = dao.findAllByName(uniqueName)
                .toStream().collect(Collectors.toList());
        assertThat(results, hasSize(1));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestElasticsearchModel> results = dao.findEvenDeletedByName(uniqueName)
                .toStream().collect(Collectors.toList());
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
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteById(model.getId()).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeleted_thenItShouldNotBeFoundAnymoreByFindIdWithPublisher() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(Mono.just(model.getId())).block(), is(notNullValue()));

        dao.deleteById(model.getId()).block();

        assertThat(dao.findById(Mono.just(model.getId())).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeletedWithDeleteByIdPublisher_thenItShouldNotBeFoundAnymore() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteById(Mono.just(model.getId())).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeletedWithDeleteAll_thenItShouldNotBeFoundAnymore() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteAll(singletonList(model)).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeletedWithDeleteAllPublisher_thenItShouldNotBeFoundAnymore() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteAll(Flux.just(model)).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByName_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        String uniqueName = UUID.randomUUID().toString();
        model.setName(uniqueName);

        model = dao.save(model).block();

        List<TestElasticsearchModel> results = dao.search(uniqueName, exactMatchSearch())
                .toStream().collect(Collectors.toList());
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByDescriptorExternalId_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());

        model = dao.save(model).block();

        List<TestElasticsearchModel> results = dao.search(model.getUuid().getExternalId(), exactMatchSearch())
                .toStream().collect(Collectors.toList());
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithoutParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = \"new name\"").block();
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId()).block();

        assertThat(foundModel.getName(), is("new name"));
    }

    @NotNull
    private TestElasticsearchModel createAModelWithOldName() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("old name");
        dao.save(model).block();
        return model;
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = params.name",
                Collections.singletonMap("name", "new name")).block();
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId()).block();

        assertThat(foundModel.getName(), is("new name"));
    }

    @Test
    void givenNoEntityExists_whenPatchingIt_thenExceptionShouldBeThrown() {
        assertThrows(BulkFailureException.class,
                () -> dao.patch(UUID.randomUUID().toString(), "ctx._source.name = \"new name\"").block());
    }

    @Test
    void givenAnEntityExists_whenPatchingIt_thenModifiedTimeShouldChange() throws Exception {
        TestElasticsearchModel originalModel = createAModelWithOldName();

        waitForAPalpableTime();

        dao.patch(originalModel.getId(), "ctx._source.name = \"new name\"").block();

        TestElasticsearchModel foundModel = dao.findById(originalModel.getId()).block();

        assertThatFoundModelModificationTimeIsAfterTheOriginalModelModificationTime(originalModel, foundModel);
    }

    private void waitForAPalpableTime() throws InterruptedException {
        Thread.sleep(100);
    }

    private void assertThatFoundModelModificationTimeIsAfterTheOriginalModelModificationTime(
            TestElasticsearchModel originalModel, TestElasticsearchModel foundModel) {
        ZonedDateTime originalModified = originalModel.getModified();
        ZonedDateTime foundModified = foundModel.getModified();
        assertTrue(foundModified.isAfter(originalModified));
    }

    @Test
    void whenAnEntityIsDeletedByObject_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        dao.delete(model).block();

        assertThatEntityWasMarkedAsDeleted(model);
    }

    private void assertThatEntityWasMarkedAsDeleted(TestElasticsearchModel model) {
        Optional<TestElasticsearchModel> foundModelOpt = client.getAsJson(TestElasticsearchModel.INDEX,
                model.getId())
                .map(this::parseJsonWithOurObjectMapper);
        assertThat("Present", foundModelOpt.isPresent(), is(true));

        TestElasticsearchModel parsedModel = foundModelOpt
                .orElseThrow(this::didNotFindAnything);
        assertThat("Marked as deleted", parsedModel.getDeleted(), is(true));
    }

    @NotNull
    private AssertionError didNotFindAnything() {
        return new AssertionError("Did not find");
    }

    private TestElasticsearchModel parseJsonWithOurObjectMapper(String json) {
        ObjectMapper mapper = new BasicJsonObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            return mapper.readerFor(TestElasticsearchModel.class).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenAnEntityIsDeletedById_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = saveAndDeleteModel();

        assertThatEntityWasMarkedAsDeleted(model);
    }

    @Test
    void whenAnEntityIsDeletedWithABatch_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        dao.deleteAll(ImmutableList.of(model)).block();

        assertThatEntityWasMarkedAsDeleted(model);
    }

    @Test
    void whenDeleteAll_thenAnExceptionShouldBeThrown() {
        try {
            dao.deleteAll().block();
            fail("An exception should be thrown");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("We don't allow to delete all the documents in one go"));
        }
    }

    @Test
    void whenSearching_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestElasticsearchModel> results = dao.search(uniqueName, exactMatchSearch())
                .toStream().collect(Collectors.toList());

        assertThat(results, hasSize(1));
    }

    @Test
    void countShouldThrowAnExceptionToAvoidFetchingAllTheEntities() {
        assertThrows(UnsupportedOperationException.class, () -> dao.count().block());
    }

    @Test
    void given1ExactFieldMatchAnd2NonExactMatchesExist_whenSearchingWithExactSemantics_then1ResultShouldBeFound() {
        ReactiveElasticsearchExactSearchTests tests = new ReactiveElasticsearchExactSearchTests(dao);
        String exactName = tests.generate1ModelWithExactNameAnd2ModelsWithReversedAndAmendedNamesAndReturnExactName();

        tests.assertThatInexactSearchYields3Results(exactName);
        tests.assertThatExactSearchYields1Result(exactName);
    }

    @NotNull
    private List<TestElasticsearchModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestElasticsearchModel notDeleted = new TestElasticsearchModel();
        notDeleted.setName(uniqueName);

        TestElasticsearchModel deleted = new TestElasticsearchModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

}
