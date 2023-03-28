package io.extremum.mongo.dao;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.mongo.model.MongoVersionedModel;
import org.bson.types.ObjectId;

public interface ReactiveMongoVersionedDao<M extends MongoVersionedModel> extends ReactiveCommonDao<M, ObjectId> {
}
