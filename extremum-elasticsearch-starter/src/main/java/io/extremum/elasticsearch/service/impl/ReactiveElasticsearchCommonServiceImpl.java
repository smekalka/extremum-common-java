package io.extremum.elasticsearch.service.impl;

import io.extremum.common.iri.service.IriFacilities;
import io.extremum.common.service.impl.ReactiveCommonServiceImpl;
import io.extremum.elasticsearch.dao.ReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.service.ReactiveElasticsearchCommonService;
import reactor.core.publisher.Flux;


public abstract class ReactiveElasticsearchCommonServiceImpl<M extends ElasticsearchCommonModel>
        extends ReactiveCommonServiceImpl<String, M> implements ReactiveElasticsearchCommonService<M> {

    private final ReactiveElasticsearchCommonDao<M> dao;

    protected ReactiveElasticsearchCommonServiceImpl(ReactiveElasticsearchCommonDao<M> dao, IriFacilities iriFacilities) {
        super(dao, iriFacilities);

        this.dao = dao;
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }

    @Override
    public Flux<M> search(String queryString, SearchOptions searchOptions) {
        return dao.search(queryString, searchOptions);
    }
}
