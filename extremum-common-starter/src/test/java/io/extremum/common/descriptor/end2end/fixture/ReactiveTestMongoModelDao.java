package io.extremum.common.descriptor.end2end.fixture;

import io.extremum.mongo.dao.impl.SpringDataReactiveMongoCommonDao;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactiveTestMongoModelDao extends SpringDataReactiveMongoCommonDao<TestMongoModel> {
}
