package io.extremum.mongo.service;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.mongo.model.MongoVersionedModel;

public interface ReactiveMongoVersionedService<M extends MongoVersionedModel> extends ReactiveCommonService<M> {
}
