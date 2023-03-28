package io.extremum.mongo.springdata.repository;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.model.QueryFields;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
abstract class BaseMongoRepository<T extends MongoCommonModel> extends SimpleMongoRepository<T, ObjectId>
        implements MongoCommonDao<T> {
    private final MongoEntityInformation<T, ObjectId> entityInformation;
    private final MongoOperations mongoOperations;

    BaseMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
    }

    @Override
    public List<T> listByParameters(Map<String, Object> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return findAll();
        }

        OptionalInt optionalLimit = OptionalInt.empty();
        OptionalInt optionalOffset = OptionalInt.empty();
        final List<Criteria> leafCriteria = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case QueryFields.LIMIT:
                    String limitStr = String.valueOf(entry.getValue());
                    int limit = Integer.valueOf(limitStr);
                    optionalLimit = OptionalInt.of(limit);
                    break;
                case QueryFields.OFFSET:
                    String offsetStr = String.valueOf(entry.getValue());
                    int offset = Integer.valueOf(offsetStr);
                    optionalOffset = OptionalInt.of(offset);
                    break;
                case QueryFields.IDS:
                    Collection ids = (Collection) entry.getValue();
                    List<ObjectId> objectIds = new ArrayList<>();
                    for (Object id : ids) {
                        objectIds.add(new ObjectId(id.toString()));
                    }
                    leafCriteria.add(where(entityInformation.getIdAttribute()).in(objectIds));
                    break;
                default:
                    leafCriteria.add(where(key).is(entry.getValue()));
                    break;
            }
        }

        final Query query;
        if (leafCriteria.isEmpty()) {
            query = notDeletedQuery();
        } else {
            query = notDeletedQueryWith(new Criteria().andOperator(leafCriteria.toArray(new Criteria[0])));
        }

        optionalOffset.ifPresent(query::skip);
        optionalLimit.ifPresent(query::limit);

        return findAllByQuery(query);
    }

    @Override
    public List<T> listByFieldValue(String fieldName, Object fieldValue) {
        return findAllByQuery(notDeletedQueryWith(where(fieldName).is(fieldValue)));
    }

    @Override
    public Optional<T> getSelectedFieldsById(ObjectId id, String... fieldNames) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = notDeletedQueryWith(getIdCriteria(id));
        Arrays.stream(fieldNames).forEach(fieldName -> query.fields().include(fieldName));

        return findOneByQuery(query);
    }

    final Optional<T> findOneByQuery(Query query) {
        T result = mongoOperations.findOne(query,
                entityInformation.getJavaType(), entityInformation.getCollectionName());
        return Optional.ofNullable(result);
    }

    final List<T> findAllByQuery(@Nullable Query query) {
   		if (query == null) {
   			return Collections.emptyList();
   		}

   		return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
   	}

    final Criteria getIdCriteria(Object id) {
        return where(entityInformation.getIdAttribute()).is(id);
    }

    abstract Query notDeletedQueryWith(Criteria criteria);

    abstract Query notDeletedQuery();
}
