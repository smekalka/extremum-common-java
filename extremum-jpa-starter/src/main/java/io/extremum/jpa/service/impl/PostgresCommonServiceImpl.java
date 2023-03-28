package io.extremum.jpa.service.impl;

import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.service.PostgresCommonService;
import io.extremum.jpa.model.PostgresCommonModel;


public class PostgresCommonServiceImpl<M extends PostgresCommonModel> extends PostgresBasicServiceImpl<M>
        implements PostgresCommonService<M> {

    public PostgresCommonServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }
}
