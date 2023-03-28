package io.extremum.elasticsearch.service;

import io.extremum.common.service.CommonService;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.util.List;

/**
 * Common interface for Elasticsearch services.
 */
public interface ElasticsearchCommonService<M extends ElasticsearchCommonModel> extends CommonService<M> {
    List<M> search(String queryString, SearchOptions searchOptions);
}
