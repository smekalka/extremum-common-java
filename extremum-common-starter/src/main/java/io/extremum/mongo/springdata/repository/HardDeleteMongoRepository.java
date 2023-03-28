package io.extremum.mongo.springdata.repository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

/**
 * @author rpuch
 */
public class HardDeleteMongoRepository<T extends MongoCommonModel> extends BaseMongoRepository<T>
        implements MongoCommonDao<T> {
    private final MongoEntityInformation<T, ObjectId> metadata;

    public HardDeleteMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
                                     MongoOperations mongoOperations) {
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
    public T deleteByIdAndReturn(ObjectId id) {
        T model = findById(id).orElseThrow(() -> new ModelNotFoundException(metadata.getJavaType(), id.toString()));

        deleteById(id);

        return model;
    }

    @Override
    public Page<T> findAll(Specification<T> specification, Pageable pageable) {
        throw new NotImplementedException();
    }
}
