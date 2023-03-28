package io.extremum.mongo.dao.impl;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataMongoCommonDao<M extends MongoCommonModel>
        extends MongoCommonDao<M>, MongoRepository<M, ObjectId> {
}
