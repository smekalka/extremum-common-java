package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
abstract class BaseReactiveMongoRepository<T extends MongoCommonModel>
        extends SimpleReactiveMongoRepository<T, ObjectId>
        implements ReactiveMongoCommonDao<T> {
    private final MongoEntityInformation<T, ObjectId> entityInformation;
    private final ReactiveMongoOperations mongoOperations;

    BaseReactiveMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
                                ReactiveMongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public final Mono<Void> deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
    }

    final Mono<T> findOneByQuery(Query query) {
        return mongoOperations.findOne(query,
                entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    final Flux<T> findAllByQuery(Query query) {
   		return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
   	}

    final Criteria getIdCriteria(Object id) {
        return where(entityInformation.getIdAttribute()).is(id);
    }

    abstract Query notDeletedQueryWith(Criteria criteria);

    abstract Query notDeletedQuery();
}
