package io.extremum.elasticsearch.service;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.exceptions.WrongArgumentException;
import io.extremum.common.utils.ModelUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.dao.TestReactiveElasticsearchModelDao;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveElasticsearchCommonServiceTest {

    @Mock
    private TestReactiveElasticsearchModelDao dao;

    @InjectMocks
    private TestReactiveElasticsearchModelService service;

    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    private TestElasticsearchModel getTestModel() {
        TestElasticsearchModel model = new TestElasticsearchModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(UUID.randomUUID().toString());

        Descriptor descriptor = Descriptor.builder()
                .externalId(uuidGenerator.generateUUID())
                .internalId(model.getId())
                .modelType(ModelUtils.getModelName(model))
                .storageType(StandardStorageType.POSTGRES)
                .build();

        model.setUuid(descriptor);

        return model;
    }

    @Test
    void testGet() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Mono.just(createdModel));

        TestElasticsearchModel resultModel = service.get(createdModel.getId()).block();
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testGetWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.get(null).block());
    }

    @Test
    void testGetWithException() {
        when(dao.findById(anyString())).thenReturn(Mono.empty());

        assertThrows(ModelNotFoundException.class, () -> service.get(UUID.randomUUID().toString()).block());
    }

    @Test
    void testCreate() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.save(any(TestElasticsearchModel.class))).thenReturn(Mono.just(createdModel));

        TestElasticsearchModel resultModel = service.create(new TestElasticsearchModel()).block();
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testCreateWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((TestElasticsearchModel) null).block());
    }

    @Test
    void testCreateList() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Flux.just(createdModel));

        List<TestElasticsearchModel> resultModels = service.create(singletonList(new TestElasticsearchModel()))
                .toStream().collect(Collectors.toList());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test
    void testCreateListWithNullData() {
        assertThrows(WrongArgumentException.class,
                () -> service.create((List<TestElasticsearchModel>) null).blockLast());
    }

    @Test
    void testSaveNewModel() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.save(any())).thenReturn(Mono.just(createdModel));
        when(dao.findById(anyString())).thenReturn(Mono.empty());

        TestElasticsearchModel resultModel = service.save(new TestElasticsearchModel()).block();
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel).block();
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testSaveUpdatedModel() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Mono.just(createdModel));

        TestElasticsearchModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        when(dao.save(updatedModel)).thenReturn(Mono.just(updatedModel));

        TestElasticsearchModel resultModel = service.save(updatedModel).block();
        assertEquals(updatedModel, resultModel);
        assertEquals(createdModel.getUuid(), resultModel.getUuid());
    }

    @Test
    void testSaveWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.save(null).block());
    }

    @Test
    void testDeleteWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.delete(null).block());
    }

    @Test
    void whenSearching_thenDaoSearchShouldBeInvoked() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        SearchOptions options = SearchOptions.builder().build();
        when(dao.search("query", options)).thenReturn(Flux.just(model));

        List<TestElasticsearchModel> results = service.search("query", options)
                .toStream().collect(Collectors.toList());
        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(sameInstance(model)));
    }
}
