package io.extremum.elasticsearch.service;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.exceptions.WrongArgumentException;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.common.service.AlertsCollector;
import io.extremum.common.utils.ModelUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.dao.TestElasticsearchModelDao;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchCommonServiceTest {

    @Mock
    private TestElasticsearchModelDao dao;

    @InjectMocks
    private TestElasticsearchModelService service;

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
        when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticsearchModel resultModel = service.get(createdModel.getId());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testGetWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.get(null));
    }

    @Test
    void testGetWithException() {
        assertThrows(ModelNotFoundException.class, () -> service.get(UUID.randomUUID().toString()));
    }

    @Test
    void testGetWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticsearchModel resultModel = service.get(createdModel.getId(), new AlertsCollector(alertList));
        assertEquals(createdModel, resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.get(UUID.randomUUID().toString(), new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("404", alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.get(null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testCreate() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.save(any(TestElasticsearchModel.class))).thenReturn(createdModel);

        TestElasticsearchModel resultModel = service.create(new TestElasticsearchModel());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testCreateWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((TestElasticsearchModel) null));
    }

    @Test
    void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.save(any(TestElasticsearchModel.class))).thenReturn(createdModel);

        TestElasticsearchModel resultModel = service.create(new TestElasticsearchModel(), new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestElasticsearchModel) null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testCreateList() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.saveAll(anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestElasticsearchModel> resultModels = service.create(Collections.singletonList(new TestElasticsearchModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test
    void testCreateListWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((List<TestElasticsearchModel>) null));
    }

    @Test
    void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.saveAll(anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestElasticsearchModel> resultModels = service.create(Collections.singletonList(new TestElasticsearchModel()), new AlertsCollector(alertList));

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestElasticsearchModel>) null, new AlertsCollector(alertList));

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testSaveNewModel() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.save(any(TestElasticsearchModel.class))).thenReturn(createdModel);

        TestElasticsearchModel resultModel = service.save(new TestElasticsearchModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testSaveUpdatedModel() {
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticsearchModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestElasticsearchModel resultModel = service.save(updatedModel);
        assertEquals(updatedModel, resultModel);
        assertEquals(createdModel.getUuid(), resultModel.getUuid());
    }

    @Test
    void testSaveWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.save(null));
    }

    @Test
    void testSaveModelWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticsearchModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticsearchModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestElasticsearchModel resultModel = service.save(updatedModel, new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertEquals(updatedModel, resultModel);

        resultModel = service.save(null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testDeleteWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.delete(null));
    }

    @Test
    void whenSearching_thenDaoSearchShouldBeInvoked() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        SearchOptions options = SearchOptions.builder().build();
        when(dao.search("query", options)).thenReturn(Collections.singletonList(model));

        List<TestElasticsearchModel> results = service.search("query", options);
        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(sameInstance(model)));
    }
}
