package io.extremum.elasticsearch.service;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import reactor.core.publisher.Flux;

/**
 * Common interface for reactive Elasticsearch services.
 */
public interface ReactiveElasticsearchCommonService<M extends ElasticsearchCommonModel>
        extends ReactiveCommonService<M> {
    Flux<M> search(String queryString, SearchOptions searchOptions);
}
