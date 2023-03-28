package io.extremum.jpa.dao;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.jpa.dao.impl.SpringDataJpaCommonDao;
import io.extremum.jpa.model.TestJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestJpaModelDao extends SpringDataJpaCommonDao<TestJpaModel> {
    List<TestJpaModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<TestJpaModel> findEvenDeletedByName(String name);

    long countByName(String name);

    @SeesSoftlyDeletedRecords
    long countEvenDeletedByName(String name);
}
