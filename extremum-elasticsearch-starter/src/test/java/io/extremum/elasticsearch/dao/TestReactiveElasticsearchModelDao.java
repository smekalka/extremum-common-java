package io.extremum.elasticsearch.dao;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.elasticsearch.dao.impl.SpringDataReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TestReactiveElasticsearchModelDao
        extends SpringDataReactiveElasticsearchCommonDao<TestElasticsearchModel> {
    Mono<TestElasticsearchModel> findOneByName(String name);

    Flux<TestElasticsearchModel> findAllByName(String name);

    @SeesSoftlyDeletedRecords
    Flux<TestElasticsearchModel> findEvenDeletedByName(String name);

    Mono<Long> countByName(String name);

    @SeesSoftlyDeletedRecords
    Mono<Long> countEvenDeletedByName(String name);
}
