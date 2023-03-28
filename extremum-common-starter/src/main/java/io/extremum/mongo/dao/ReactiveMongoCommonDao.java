package io.extremum.mongo.dao;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;

public interface ReactiveMongoCommonDao<M extends MongoCommonModel> extends ReactiveCommonDao<M, ObjectId> {
}
