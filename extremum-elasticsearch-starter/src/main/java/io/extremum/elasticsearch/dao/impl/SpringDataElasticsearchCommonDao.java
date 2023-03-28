package io.extremum.elasticsearch.dao.impl;

import io.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataElasticsearchCommonDao<M extends ElasticsearchCommonModel>
        extends ElasticsearchCommonDao<M>, ElasticsearchRepository<M, String> {

    <N extends M> List<N> saveAll(Iterable<N> entities);
}
