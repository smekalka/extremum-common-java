package io.extremum.elasticsearch.service;

import io.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import io.extremum.elasticsearch.service.impl.ElasticsearchCommonServiceImpl;
import io.extremum.elasticsearch.model.TestElasticsearchModel;

/**
 * @author rpuch
 */
public class TestElasticsearchModelService extends ElasticsearchCommonServiceImpl<TestElasticsearchModel> {
    public TestElasticsearchModelService(ElasticsearchCommonDao<TestElasticsearchModel> dao) {
        super(dao);
    }
}
