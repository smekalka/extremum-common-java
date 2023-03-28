package io.extremum.elasticsearch.dao;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.elasticsearch.dao.impl.SpringDataElasticsearchCommonDao;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestElasticsearchModelDao extends SpringDataElasticsearchCommonDao<TestElasticsearchModel> {
    List<TestElasticsearchModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<TestElasticsearchModel> findEvenDeletedByName(String name);

    long countByName(String name);

    @SeesSoftlyDeletedRecords
    long countEvenDeletedByName(String name);
}
