package io.extremum.dynamic.dao;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.VersionedModel;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.mongo.MongoConstants;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static reactor.core.publisher.Mono.*;

@RequiredArgsConstructor
public class MongoVersionedDynamicModelDao implements JsonDynamicModelDao {
    private static final long INITIAL_VERSION_VALUE = 1L;
    private static final String PRIMARY_KEY_FIELD_NAME = "_id";

    private final ReactiveMongoOperations operations;
    private final ReactiveMongoDescriptorFacilities descriptorFacilities;

    @Override
    public Mono<JsonDynamicModel> create(JsonDynamicModel model, String collectionName) {
        return makeDescriptor(model.getModelName(), model.getIri())
                .flatMap(provideServiceFields(model))
                .flatMap(insertModel(collectionName));
    }

    private Function<JsonDynamicModel, Mono<JsonDynamicModel>> insertModel(String collectionName) {
        return model -> Mono.from(operations.getCollection(collectionName)
                .flatMap(collection -> {
                    Publisher<InsertOneResult> publisher = collection.insertOne(
                            new Document(model.getModelData()));
                    return Mono.from(publisher);
                }))
                .thenReturn(model);
    }

    private Function<Descriptor, Mono<JsonDynamicModel>> provideServiceFields(JsonDynamicModel model) {
        return descr -> fromSupplier(() -> {
            ObjectId oId = new ObjectId(descr.getInternalId());
            ZonedDateTime now = ZonedDateTime.now();

            model.setId(descr);
            model.getModelData().put(Model.FIELDS.version.name(), INITIAL_VERSION_VALUE);
            model.getModelData().put(Model.FIELDS.created.name(), toDate(now));
            model.getModelData().put(Model.FIELDS.modified.name(), toDate(now));
            model.getModelData().put(Model.FIELDS.model.name(), model.getModelName());
            model.getModelData().put(VersionedModel.FIELDS.lineageId.name(), oId);
            model.getModelData().put(VersionedModel.FIELDS.currentSnapshot.name(), true);
            model.getModelData().put(VersionedModel.FIELDS.start.name(), toDate(now));
            model.getModelData().put(VersionedModel.FIELDS.end.name(), toDate(MongoConstants.DISTANT_FUTURE));

            return model;
        });
    }

    private Date toDate(ZonedDateTime zdt) {
        return Date.from(zdt.toInstant());
    }

    private Mono<Descriptor> makeDescriptor(String modelName, String iri) {
        return descriptorFacilities.createOrGet(new ObjectId().toString(), modelName, iri);
    }

    @Override
    @Transactional
    public Mono<JsonDynamicModel> update(JsonDynamicModel updatedModel, String collectionName) {
        return getByIdFromCollection(updatedModel.getId(), collectionName)
                .flatMap(currentSnapshot -> {
                    Date now = toDate(ZonedDateTime.now());

                    Tuple2<JsonDynamicModel, JsonDynamicModel> prepared = prepareCurrentAndNextSnapshots(currentSnapshot, updatedModel, now);
                    JsonDynamicModel current = prepared.getT1();
                    JsonDynamicModel next = prepared.getT2();

                    Bson filter = and(
                            eq(PRIMARY_KEY_FIELD_NAME, current.getModelData().get(PRIMARY_KEY_FIELD_NAME))
                    );

                    Bson update = combine(
                            set(VersionedModel.FIELDS.currentSnapshot.name(), false),
                            set(VersionedModel.FIELDS.end.name(), now)
                    );

                    return operations.getCollection(collectionName)
                            .flatMap(collection -> {
                                Publisher<UpdateResult> publisher = collection.updateOne(filter, update);
                                return Mono.from(publisher);
                            }).then(insertSnapshot(next, collectionName));
                });
    }

    private Mono<JsonDynamicModel> insertSnapshot(JsonDynamicModel snapshot, String collectionName) {
        return operations.getCollection(collectionName)
                .flatMap(collection -> {
                    Publisher<InsertOneResult> publisher = collection.insertOne(
                            new Document(snapshot.getModelData()));
                    return Mono.from(publisher);
                }).thenReturn(snapshot);
    }

    private Tuple2<JsonDynamicModel, JsonDynamicModel> prepareCurrentAndNextSnapshots(JsonDynamicModel currentSnapshot, JsonDynamicModel updatedModel, Date now) {
        JsonDynamicModel current = cloneModel(currentSnapshot);
        JsonDynamicModel next = cloneModel(updatedModel);

        current.getModelData().put(VersionedModel.FIELDS.end.name(), now);
        current.getModelData().put(VersionedModel.FIELDS.currentSnapshot.name(), false);

        next.getModelData().put(Model.FIELDS.version.name(), extractVersion(currentSnapshot) + 1);
        next.getModelData().put(VersionedModel.FIELDS.lineageId.name(), new ObjectId(currentSnapshot.getId().getInternalId()));
        next.getModelData().put(VersionedModel.FIELDS.currentSnapshot.name(), true);
        next.getModelData().put(Model.FIELDS.created.name(), currentSnapshot.getModelData().get(Model.FIELDS.created.name()));
        next.getModelData().put(Model.FIELDS.modified.name(), now);
        next.getModelData().put(VersionedModel.FIELDS.start.name(), now);
        next.getModelData().put(VersionedModel.FIELDS.end.name(), toDate(MongoConstants.DISTANT_FUTURE));

        next.getModelData().remove(PRIMARY_KEY_FIELD_NAME);

        return Tuples.of(current, next);
    }

    private JsonDynamicModel cloneModel(JsonDynamicModel currentSnapshot) {
        return new JsonDynamicModel(currentSnapshot.getId(), currentSnapshot.getModelName(), new HashMap<>(currentSnapshot.getModelData()));
    }

    private long extractVersion(JsonDynamicModel model) {
        return (long) model.getModelData().get(Model.FIELDS.version.name());
    }

    @Override
    public Mono<JsonDynamicModel> getByIdFromCollection(Descriptor id, String collectionName) {
        return extractInternalId(id)
                .flatMap(getActiveSnapshotFromCollection(collectionName))
                .map(doc -> new JsonDynamicModel(id, doc.getString(Model.FIELDS.model.name()), doc))
                .switchIfEmpty(defer(() -> error(new ModelNotFoundException("Dynamic model with id " + id + " isn't found"))));
    }

    private Function<ObjectId, Mono<Document>> getActiveSnapshotFromCollection(String collectionName) {
        return oId -> {
            Bson condition = and(
                    eq(VersionedModel.FIELDS.lineageId.name(), oId),
                    eq(VersionedModel.FIELDS.currentSnapshot.name(), true),
                    or(
                            eq(Model.FIELDS.deleted.name(), false),
                            exists(Model.FIELDS.deleted.name(), false)
                    )
            );

            return operations.getCollection(collectionName)
                    .flatMap(collection -> {
                        Publisher<Document> publisher = collection.find(condition).first();
                        return Mono.from(publisher);
                    });
        };
    }

    @Override
    @Transactional
    public Mono<Void> remove(Descriptor id, String collectionName) {
        return getByIdFromCollection(id, collectionName)
                .flatMap(found -> {
                    found.getModelData().put(Model.FIELDS.deleted.name(), true);
                    return update(found, collectionName);
                }).then();
    }

    private Mono<ObjectId> extractInternalId(Descriptor id) {
        return id.getInternalIdReactively()
                .map(ObjectId::new);
    }
}
