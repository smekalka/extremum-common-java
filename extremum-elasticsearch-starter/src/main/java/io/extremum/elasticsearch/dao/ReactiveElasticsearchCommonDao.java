package io.extremum.elasticsearch.dao;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ReactiveElasticsearchCommonDao<M extends ElasticsearchCommonModel>
        extends ReactiveCommonDao<M, String> {

    Flux<M> search(String queryString, SearchOptions searchOptions);

    Mono<Boolean> patch(String id, String painlessScript);

    Mono<Boolean> patch(String id, String painlessScript, Map<String, Object> scriptParams);
}
