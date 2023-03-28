package io.extremum.elasticsearch.dao.impl;

import io.extremum.elasticsearch.dao.ReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataReactiveElasticsearchCommonDao<M extends ElasticsearchCommonModel>
        extends ReactiveElasticsearchCommonDao<M>, ReactiveElasticsearchRepository<M, String> {
}
