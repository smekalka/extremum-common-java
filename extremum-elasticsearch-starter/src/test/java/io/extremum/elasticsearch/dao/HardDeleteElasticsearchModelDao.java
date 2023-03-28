package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.dao.impl.SpringDataElasticsearchCommonDao;
import io.extremum.elasticsearch.model.HardDeleteElasticsearchModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteElasticsearchModelDao
        extends SpringDataElasticsearchCommonDao<HardDeleteElasticsearchModel> {
    List<HardDeleteElasticsearchModel> findByName(String name);

    long countByName(String name);
}
