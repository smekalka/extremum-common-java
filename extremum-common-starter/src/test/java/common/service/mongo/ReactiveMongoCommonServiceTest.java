package common.service.mongo;

import common.dao.mongo.TestReactiveMongoModelDao;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.exceptions.WrongArgumentException;
import io.extremum.common.utils.ModelUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReactiveMongoCommonServiceTest {

    private final TestReactiveMongoModelDao dao = mock(TestReactiveMongoModelDao.class);
    private final TestReactiveMongoModelService service = new TestReactiveMongoModelService(dao);

    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    @Test
    void testGet() {
        TestMongoModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Mono.just(createdModel));

        TestMongoModel resultModel = service.get(createdModel.getId().toString()).block();
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testGetWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.get(null).block());
    }

    @Test
    void testGetWithException() {
        when(dao.findById(any(ObjectId.class))).thenReturn(Mono.empty());

        assertThrows(ModelNotFoundException.class, () -> service.get(new ObjectId().toString()).block());
    }

    @Test
    void testCreate() {
        TestMongoModel createdModel = getTestModel();
        when(dao.save(any(TestMongoModel.class))).thenReturn(Mono.just(createdModel));

        TestMongoModel resultModel = service.create(new TestMongoModel()).block();
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testCreateWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((TestMongoModel) null).block());
    }

    @Test
    void testCreateList() {
        TestMongoModel createdModel = getTestModel();
        when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Flux.just(createdModel));

        List<TestMongoModel> resultModels = service.create(Collections.singletonList(new TestMongoModel()))
                .toStream().collect(Collectors.toList());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test
    void testCreateListWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((List<TestMongoModel>) null).blockLast());
    }

    @Test
    void testSaveNewModel() {
        TestMongoModel createdModel = getTestModel();
        when(dao.findById(any(ObjectId.class))).thenReturn(Mono.just(createdModel));
        when(dao.save(any(TestMongoModel.class))).thenReturn(Mono.just(createdModel));

        TestMongoModel resultModel = service.save(new TestMongoModel()).block();
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel).block();
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testSaveUpdatedModel() {
        TestMongoModel createdModel = getTestModel();
        when(dao.findById(createdModel.getId())).thenReturn(Mono.just(createdModel));

        TestMongoModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        when(dao.save(updatedModel)).thenReturn(Mono.just(updatedModel));

        TestMongoModel resultModel = service.save(updatedModel).block();
        assertNotNull(resultModel);
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
