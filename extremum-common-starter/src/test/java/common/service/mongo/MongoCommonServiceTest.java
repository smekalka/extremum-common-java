package common.service.mongo;

import common.dao.mongo.TestMongoModelDao;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.exceptions.WrongArgumentException;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.common.service.AlertsCollector;
import io.extremum.common.service.Problems;
import io.extremum.common.utils.ModelUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.*;

import static io.extremum.common.model.PersistableCommonModel.FIELDS.version;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MongoCommonServiceTest {

    private final TestMongoModelDao dao = Mockito.mock(TestMongoModelDao.class);
    private final TestMongoModelService service = new TestMongoModelService(dao);

    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    @Test
    void testGet() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel resultModel = service.get(createdModel.getId().toString());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testGetWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.get(null));
    }

    @Test
    void testGetWithException() {
        assertThrows(ModelNotFoundException.class, () -> service.get(new ObjectId().toString()));
    }

    @Test
    void testGetWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel resultModel = service.get(createdModel.getId().toString(), new AlertsCollector(alertList));
        assertEquals(createdModel, resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.get(new ObjectId().toString(), new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("not-found", alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.get(null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testListWithParameters() {
        TestMongoModel createdModel = getTestModel();
        Map<String, Object> params = Collections.singletonMap("offset", 1);
        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());

        List<TestMongoModel> resultModelList = service.listByParameters(null);
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByParameters(params);
        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    void testListWithParametersWithAlerts() {
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Map<String, Object> params = Collections.singletonMap("offset", 1);
        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());

        List<TestMongoModel> resultModelList = service.listByParameters(null, new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByParameters(params, new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    void testListByFieldValue() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
                .thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    void testListByFieldValueWithNullName() {
        assertThrows(WrongArgumentException.class, () -> service.listByFieldValue(null, ""));
    }

    @Test
    void testListByFieldValueWithNullValue() {
        assertThrows(WrongArgumentException.class, () -> service.listByFieldValue(version.name(), null));
    }

    @Test
    void testListByFieldValueWithAlerts() {
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
                .thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByFieldValue(null, null, new AlertsCollector(alertList));
        assertEquals(2, alertList.size());
        assertEquals("400", alertList.get(0).getCode());
        assertEquals("400", alertList.get(1).getCode());

        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    void testListByFieldValueWithLimitOffset() {
        TestMongoModel createdModel = getTestModel();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("offset", 1);
        params.put(version.name(), createdModel.getVersion());

        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1);
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    void testListByFieldValueWithLimitOffsetWithNullName() {
        assertThrows(WrongArgumentException.class, () -> service.listByFieldValue(null, "", 0, 0));
    }

    @Test
    void testListByFieldValueWithLimitOffsetWithNullValue() {
        assertThrows(WrongArgumentException.class, () -> service.listByFieldValue(version.name(), null, 0, 0));
    }

    @Test
    void testListByFieldValueWithLimitOffsetWithAlerts() {
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("offset", 1);
        params.put(version.name(), createdModel.getVersion());

        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList =
                service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1, new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByFieldValue(null, null, 0, 0, new AlertsCollector(alertList));

        assertEquals(2, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertTrue(alertList.get(1).isError());
        assertEquals("400", alertList.get(0).getCode());
        assertEquals("400", alertList.get(1).getCode());

        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    void testGetSelectedFieldsById() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.getSelectedFieldsById(createdModel.getId(), version.name())).thenReturn(
                Optional.of(createdModel));

        TestMongoModel resultModel = service.getSelectedFieldsById(createdModel.getId().toString(), version.name());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testGetSelectedFieldsByIdWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.getSelectedFieldsById(null, version.name()));
    }

    @Test
    void testGetSelectedFieldsByIdWithNullFields() {
        assertThrows(WrongArgumentException.class, () -> service.getSelectedFieldsById(new ObjectId().toString()));
    }

    @Test
    void testGetSelectedFieldsByIdWithException() {
        assertThrows(ModelNotFoundException.class, () -> service.getSelectedFieldsById(new ObjectId().toString(), version.name()));
    }

    @Test
    void testGetSelectedFieldsByIdWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestMongoModel resultModel = service.getSelectedFieldsById(new ObjectId().toString(), new AlertsCollector(alertList), version.name());

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("not-found", alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.getSelectedFieldsById(null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(2, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertTrue(alertList.get(1).isError());
        assertEquals("400", alertList.get(0).getCode());
        assertEquals("400", alertList.get(1).getCode());
    }

    @Test
    void testCreate() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestMongoModel.class))).thenReturn(createdModel);

        TestMongoModel resultModel = service.create(new TestMongoModel());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testCreateWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((TestMongoModel) null));
    }

    @Test
    void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestMongoModel.class))).thenReturn(createdModel);

        TestMongoModel resultModel = service.create(new TestMongoModel(), new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestMongoModel) null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testCreateList() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModels = service.create(Collections.singletonList(new TestMongoModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test
    void testCreateListWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((List<TestMongoModel>) null));
    }

    @Test
    void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModels = service.create(Collections.singletonList(new TestMongoModel()), new AlertsCollector(alertList));

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestMongoModel>) null, new AlertsCollector(alertList));

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testSaveNewModel() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestMongoModel.class))).thenReturn(createdModel);

        TestMongoModel resultModel = service.save(new TestMongoModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testSaveUpdatedModel() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestMongoModel resultModel = service.save(updatedModel);
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
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestMongoModel resultModel = service.save(updatedModel, new AlertsCollector(alertList));
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
    void whenNullCollectionAlertsIsPassedToGet_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(() -> service.get("id", null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToCreate_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.create(new TestMongoModel(), null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToCreateList_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.create(Collections.emptyList(), null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToSave_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.save(new TestMongoModel(), null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToDelete_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.delete("id", null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToListByParameters_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.listByParameters(Collections.emptyMap(), null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToListByFieldValue_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.listByFieldValue("key", "value", null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToListByFieldValueWithPaging_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.listByFieldValue("key", "value", 0, 10, null));
    }

    @Test
    void whenNullCollectionAlertsIsPassedToGetSelectedFieldsById_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.getSelectedFieldsById("id", (Problems) null));
    }

    private void makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(Runnable runnable) {
        try {
            runnable.run();
            fail("An exception should be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Problems must not be null"));
        }
    }

    private TestMongoModel getTestModel() {
        TestMongoModel model = new TestMongoModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(new ObjectId());

        String modelName = ModelUtils.getModelName(model.getClass());

        Descriptor descriptor = Descriptor.builder()
                .externalId(uuidGenerator.generateUUID())
                .internalId(model.getId().toString())
                .modelType(modelName)
                .storageType(StandardStorageType.MONGO)
                .build();

        model.setUuid(descriptor);

        return model;
    }
}
