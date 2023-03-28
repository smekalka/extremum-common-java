package io.extremum.mongo.dao.impl;

import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataReactiveMongoCommonDao<M extends MongoCommonModel>
        extends ReactiveMongoCommonDao<M>, ReactiveMongoRepository<M, ObjectId> {
}
