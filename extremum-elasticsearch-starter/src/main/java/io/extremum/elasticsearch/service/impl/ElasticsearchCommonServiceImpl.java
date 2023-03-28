package io.extremum.elasticsearch.service.impl;

import io.extremum.common.service.impl.CommonServiceImpl;
import io.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.service.ElasticsearchCommonService;

import java.util.List;


public abstract class ElasticsearchCommonServiceImpl<M extends ElasticsearchCommonModel> extends CommonServiceImpl<String, M>
        implements ElasticsearchCommonService<M> {

    private final ElasticsearchCommonDao<M> dao;

    protected ElasticsearchCommonServiceImpl(ElasticsearchCommonDao<M> dao) {
        super(dao);

        this.dao = dao;
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }

    @Override
    public List<M> search(String queryString, SearchOptions searchOptions) {
        return dao.search(queryString, searchOptions);
    }
}
