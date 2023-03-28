package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.dao.impl.SpringDataReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.model.HardDeleteElasticsearchModel;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface HardDeleteReactiveElasticsearchModelDao
        extends SpringDataReactiveElasticsearchCommonDao<HardDeleteElasticsearchModel> {
    Flux<HardDeleteElasticsearchModel> findByName(String name);

    Mono<Long> countByName(String name);
}
