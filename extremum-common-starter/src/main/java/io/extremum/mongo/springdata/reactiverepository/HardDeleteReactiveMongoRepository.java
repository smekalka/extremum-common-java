package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class HardDeleteReactiveMongoRepository<T extends MongoCommonModel> extends BaseReactiveMongoRepository<T>
        implements ReactiveMongoCommonDao<T> {
    private final MongoEntityInformation<T, ObjectId> metadata;

    public HardDeleteReactiveMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
                                             ReactiveMongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.metadata = metadata;
    }

    @Override
    Query notDeletedQueryWith(Criteria criteria) {
        return new Query(criteria);
    }

    @Override
    Query notDeletedQuery() {
        return new Query();
    }

    @Override
    public Mono<T> deleteByIdAndReturn(ObjectId id) {
        return findById(id)
                .flatMap(found -> deleteById(id).thenReturn(found))
                .switchIfEmpty(Mono.error(new ModelNotFoundException(metadata.getJavaType(), id.toString())));
    }
}
