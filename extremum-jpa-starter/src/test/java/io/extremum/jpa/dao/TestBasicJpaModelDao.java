package io.extremum.jpa.dao;

import io.extremum.jpa.dao.impl.SpringDataJpaCommonDao;
import io.extremum.jpa.model.TestBasicJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestBasicJpaModelDao extends SpringDataJpaCommonDao<TestBasicJpaModel> {
    List<TestBasicJpaModel> findByName(String name);
}
