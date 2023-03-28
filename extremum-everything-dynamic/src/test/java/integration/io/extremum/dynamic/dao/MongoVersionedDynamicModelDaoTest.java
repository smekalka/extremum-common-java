package integration.io.extremum.dynamic.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.Function;
import integration.SpringBootTestWithServices;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.VersionedModel;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.basic.Model;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static io.extremum.common.model.VersionedModel.FIELDS.*;
import static io.extremum.dynamic.DynamicModelSupports.*;
import static io.extremum.dynamic.utils.DynamicModelTestUtils.*;
import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static java.util.Arrays.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Flux.*;

@SpringBootTest(classes = MongoVersionedDynamicModelDaoTestConfiguration.class)
class MongoVersionedDynamicModelDaoTest extends SpringBootTestWithServices {
    @Autowired
    private JsonDynamicModelDao dao;

    @Autowired
    private ReactiveMongoOperations ops;

    @Test
    void versionFieldEqualsWith_1_after_creation() {
        JsonDynamicModel created = persistModel("createdModelHaveVersion_1Model", "{\"a\":\"b\"}").block();

        assertEquals(1, (Long) created.getModelData().get(Model.FIELDS.version.name()));
    }

    @Test
    void serviceFieldArePresented_after_creation() {
        JsonDynamicModel created = persistModel("serviceFieldArePresented_after_creation", "{\"a\":\"b\"}").block();


        assertThat(created.getModelData().get(Model.FIELDS.created.name()), instanceOf(Date.class));
        assertThat(created.getModelData().get(modified.name()), instanceOf(Date.class));
        assertThat(created.getModelData().get(Model.FIELDS.version.name()), instanceOf(Long.class));
        assertThat(created.getModelData().get(lineageId.name()), instanceOf(ObjectId.class));

        assertTrue(created.getModelData().containsKey(Model.FIELDS.created.name()));
        assertTrue(created.getModelData().containsKey(modified.name()));
        assertTrue(created.getModelData().containsKey(Model.FIELDS.version.name()));
        assertTrue(created.getModelData().containsKey(start.name()));
        assertTrue(created.getModelData().containsKey(end.name()));
        assertTrue(created.getModelData().containsKey(currentSnapshot.name()));
        assertTrue(created.getModelData().containsKey(lineageId.name()));
    }

    @Test
    void versionIncrement_after_update() {
        JsonDynamicModel persisted = persistModel("versionIncrement_after_updateTestModel", "{\"a\":\"b\"}").block();
        JsonDynamicModel updated = updateModel(persisted).block();
        JsonDynamicModel updated2 = updateModel(updated).block();

        assertEquals(2, (long) updated.getModelData().get(VersionedModel.FIELDS.version.name()));
        assertEquals(3, (long) updated2.getModelData().get(VersionedModel.FIELDS.version.name()));
    }

    @Test
    void snapshotTaken_after_update() {
        JsonDynamicModel persisted = persistModel("snapshotTaken_after_update", "{\"a\":\"b\"}").block();
        updateModel(persisted).block();

        String collectionName = collectionNameFromModel(persisted.getModelName());

        Long totalSnapshotsCount = findInCollection(
                collectionName,
                new Document(lineageId.name(), new ObjectId(persisted.getId().getInternalId())),
                p -> from(p).count().block());

        Bson andQuery = andQuery(
                new Document(lineageId.name(), new ObjectId(persisted.getId().getInternalId())),
                new Document(currentSnapshot.name(), true)
        );

        Long currentSnapshotsCount = findInCollection(collectionName,
                andQuery,
                p -> from(p).count().block());

        assertEquals(2, totalSnapshotsCount);
        assertEquals(1, currentSnapshotsCount);
    }

    @Test
    void findById() {
        JsonDynamicModel persisted = persistModel("findByIdTestModel", "{\"a\": \"b\"}").block();
        JsonDynamicModel found = dao.getByIdFromCollection(persisted.getId(),
                collectionNameFromModel(persisted.getModelName())).block();

        assertNotNull(found);

        assertThat(found.getModelData().get(Model.FIELDS.created.name()), instanceOf(Date.class));
        assertThat(found.getModelData().get(modified.name()), instanceOf(Date.class));
        assertThat(found.getModelData().get(Model.FIELDS.version.name()), instanceOf(Long.class));
        assertThat(found.getModelData().get(lineageId.name()), instanceOf(ObjectId.class));

        assertEquals(persisted.getModelData().get(lineageId.name()), found.getModelData().get(lineageId.name()));
        assertEquals(true, found.getModelData().get(currentSnapshot.name()));
    }

    @Test
    void activeSnapshotFoundById() {
        JsonDynamicModel persisted = persistModel("activeSnapshotFoundByIdTest", "{\"a\":\"b\"}").block();
        JsonDynamicModel updated1 = updateModel(persisted).block();
        updateModel(updated1).block();

        String collectionName = collectionNameFromModel(persisted.getModelName());

        JsonDynamicModel found = dao.getByIdFromCollection(persisted.getId(), collectionName).block();

        assertNotNull(found);
        assertEquals(found.getModelData().get(lineageId.name()), persisted.getModelData().get(lineageId.name()));
        assertEquals(true, found.getModelData().get(currentSnapshot.name()));
    }

    @Test
    void deletedModelNotFoundTest() {
        JsonDynamicModel persisted = persistModel("deletedModelNotFoundTestModel", "{\"a\":\"b\"}").block();

        String collectionName = collectionNameFromModel(persisted.getModelName());

        dao.remove(persisted.getId(), collectionName).block();

        assertThrows(ModelNotFoundException.class, () -> dao.getByIdFromCollection(persisted.getId(), collectionName).block());
    }

    @Test
    void removeTest() {
        JsonDynamicModel persisted = persistModel("removeTestModel", "{\"a\": \"b\"}").block();

        String collectionName = collectionNameFromModel(persisted.getModelName());
        dao.remove(persisted.getId(), collectionName).block();

        Bson andQuery = andQuery(
                new Document(lineageId.name(), new ObjectId(persisted.getId().getInternalId())),
                new Document(Model.FIELDS.deleted.name(), true)
        );

        Long removed = findInCollection(collectionName, andQuery, p -> from(p).count()).block();

        assertEquals(1, removed);
    }

    @Test
    void markSnapshotsAsDeleted_after_delete() {
        JsonDynamicModel persisted = persistModel("markSnapshotsAsDeleted_after_deleteModel", "{\"a\":\"b\"}").block();
        updateModel(persisted).block();

        String collectionName = collectionNameFromModel(persisted.getModelName());
        dao.remove(persisted.getId(), collectionName).block();

        long markDeletedSnapshotsFound = findInCollection(
                collectionName,
                andQuery(
                        new Document(lineageId.name(), new ObjectId(persisted.getId().getInternalId())),
                        new Document(Model.FIELDS.deleted.name(), true)
                ),
                p -> from(p).count().block()
        );

        assertEquals(1, markDeletedSnapshotsFound);
    }

    private <T> T findInCollection(String collectionName, Bson query, Function<Flux<Document>, T> transformer) {
        Flux<Document> flux = ops.getCollection(collectionName)
                .flatMapMany(collection -> Flux.from(collection.find(query)));
        return transformer.apply(flux);
    }

    private Bson andQuery(Document... andConditions) {
        List<Bson> criteria = asList(andConditions);
        return new BasicDBObject("$and", criteria);
    }

    private Mono<JsonDynamicModel> persistModel(final String modelName, final String data) {
        JsonDynamicModel model = new JsonDynamicModel(modelName, toMap(data));
        return dao.create(model, collectionNameFromModel(model.getModelName()));
    }

    private Mono<JsonDynamicModel> updateModel(JsonDynamicModel model) {
        return dao.update(model, collectionNameFromModel(model.getModelName()));
    }
}
