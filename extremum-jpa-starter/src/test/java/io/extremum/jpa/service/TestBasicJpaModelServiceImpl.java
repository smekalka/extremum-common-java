package io.extremum.jpa.service;

import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.model.TestBasicJpaModel;
import io.extremum.jpa.service.impl.PostgresBasicServiceImpl;

/**
 * @author rpuch
 */
public class TestBasicJpaModelServiceImpl extends PostgresBasicServiceImpl<TestBasicJpaModel>
        implements TestBasicJpaModelService {
    public TestBasicJpaModelServiceImpl(PostgresCommonDao<TestBasicJpaModel> dao) {
        super(dao);
    }
}
