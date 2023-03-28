package io.extremum.jpa.service;

import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.model.TestJpaModel;
import io.extremum.jpa.service.impl.PostgresCommonServiceImpl;

/**
 * @author rpuch
 */
public class TestJpaModelService extends PostgresCommonServiceImpl<TestJpaModel> {
    public TestJpaModelService(PostgresCommonDao<TestJpaModel> dao) {
        super(dao);
    }
}
