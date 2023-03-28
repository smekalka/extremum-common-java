package common.service.mongo;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import models.TestMongoModel;

/**
 * @author rpuch
 */
public class TestMongoModelService extends MongoCommonServiceImpl<TestMongoModel> {
    public TestMongoModelService(MongoCommonDao<TestMongoModel> dao) {
        super(dao);
    }
}
