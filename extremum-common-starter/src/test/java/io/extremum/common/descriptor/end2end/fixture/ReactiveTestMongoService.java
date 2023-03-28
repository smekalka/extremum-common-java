package io.extremum.common.descriptor.end2end.fixture;

import io.extremum.mongo.service.ReactiveMongoCommonService;
import reactor.core.publisher.Flux;

public interface ReactiveTestMongoService extends ReactiveMongoCommonService<TestMongoModel> {

    Flux<TestMongoModel> getModels();

}
