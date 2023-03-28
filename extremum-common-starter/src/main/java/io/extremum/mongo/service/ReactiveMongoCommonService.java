package io.extremum.mongo.service;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.mongo.model.MongoCommonModel;

/**
 * Common reactive interface for mongo services
 */
public interface ReactiveMongoCommonService<M extends MongoCommonModel> extends ReactiveCommonService<M> {
}
